package com.forgeai.core.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgeai.core.persistence.AgentRun;
import com.forgeai.core.persistence.AgentRunRepository;
import com.forgeai.core.persistence.Conversation;
import com.forgeai.core.persistence.Message;
import com.forgeai.core.persistence.Task;
import com.forgeai.core.persistence.TaskRepository;
import com.forgeai.core.persistence.ToolCall;
import com.forgeai.core.persistence.ToolCallRepository;
import com.forgeai.core.tools.ForgeTool;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.forgeai.core.persistence.Feedback;
import com.forgeai.core.persistence.FeedbackRepository;

@Service
public class AgentOrchestrator {

    private final ChatModel chatModel;
    private final ToolCallRepository toolCallRepository;
    private final TaskRepository taskRepository;
    private final AgentRunRepository agentRunRepository;
    private final FeedbackRepository feedbackRepository;
    private final Map<String, ForgeTool> tools;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BeanOutputConverter<AgentAction> outputConverter;

    public AgentOrchestrator(ChatModel chatModel, ToolCallRepository toolCallRepository, 
                             TaskRepository taskRepository, AgentRunRepository agentRunRepository,
                             FeedbackRepository feedbackRepository,
                             List<ForgeTool> toolList) {
        this.chatModel = chatModel;
        this.toolCallRepository = toolCallRepository;
        this.taskRepository = taskRepository;
        this.agentRunRepository = agentRunRepository;
        this.feedbackRepository = feedbackRepository;
        this.tools = toolList.stream().collect(Collectors.toMap(ForgeTool::getName, t -> t));
        this.outputConverter = new BeanOutputConverter<>(AgentAction.class);
    }

    public String run(Conversation conversation, AgentRun run) {
        String toolsSchema = tools.values().stream()
                .map(t -> "- " + t.getName() + ": " + t.getDescription())
                .collect(Collectors.joining("\n"));

        String format = outputConverter.getFormat();
        
        // --- Phase 11: Episodic Memory ---
        // Fetch past tool calls for this conversation
        StringBuilder memoryBuilder = new StringBuilder();
        List<Task> tasks = taskRepository.findByConversationId(conversation.getId());
        for (Task t : tasks) {
            List<AgentRun> runs = agentRunRepository.findByTaskId(t.getId());
            for (AgentRun r : runs) {
                // don't include the current run since it has no tool calls yet
                if (r.getId().equals(run.getId())) continue;
                
                List<ToolCall> calls = toolCallRepository.findByAgentRunId(r.getId());
                for (ToolCall c : calls) {
                    memoryBuilder.append("Tool Used: ").append(c.getToolName()).append("\n");
                    memoryBuilder.append("Arguments: ").append(c.getArgumentsJson()).append("\n");
                    memoryBuilder.append("Result: ").append(c.getResultText()).append("\n\n");
                }
            }
        }
        
        String memoryContext = memoryBuilder.isEmpty() ? "No past tool executions in this conversation." 
                : "PAST EPISODIC MEMORY (Do not repeat failed tools):\n" + memoryBuilder.toString();

        // --- Phase 12: Feedback-Driven Learning ---
        StringBuilder feedbackBuilder = new StringBuilder();
        List<Feedback> feedbackList = feedbackRepository.findByConversationId(conversation.getId());
        for (Feedback f : feedbackList) {
            if (!f.getIsPositive() && f.getCorrectionText() != null && !f.getCorrectionText().isBlank()) {
                feedbackBuilder.append("- ").append(f.getCorrectionText()).append("\n");
            }
        }
        String feedbackContext = feedbackBuilder.isEmpty() ? "" 
                : "\nUSER RULES & CORRECTIONS (YOU MUST OBEY THESE STRICTLY):\n" + feedbackBuilder.toString();

        String systemPromptText = """
                You are ForgeAI, an advanced agent capable of multi-step reasoning and tool use.
                Answer the user's request, considering the conversation history.
                
                %s
                %s
                
                You have access to the following tools:
                %s
                
                FORMAT INSTRUCTIONS:
                You must follow the format strictly. If you want to use a tool, provide 'toolName' and 'toolArguments'.
                If you have reached the final answer and no more tools are needed, provide 'finalAnswer'.
                IMPORTANT: You MUST return ONLY valid JSON. DO NOT wrap the JSON in markdown blocks (e.g. ```json). DO NOT include any conversational text before or after the JSON object.
                %s
                """.formatted(memoryContext, feedbackContext, toolsSchema, format);

        List<org.springframework.ai.chat.messages.Message> springAiMessages = new ArrayList<>();
        springAiMessages.add(new SystemMessage(systemPromptText));

        for (Message msg : conversation.getMessages()) {
            if ("USER".equals(msg.getRole())) {
                springAiMessages.add(new UserMessage(msg.getContent()));
            } else if ("ASSISTANT".equals(msg.getRole())) {
                springAiMessages.add(new AssistantMessage(msg.getContent()));
            } else if ("SYSTEM".equals(msg.getRole())) {
                springAiMessages.add(new SystemMessage(msg.getContent()));
            }
        }

        int maxIterations = 5;
        int currentIteration = 0;

        while (currentIteration < maxIterations) {
            // Use JSON format strictly!
            Prompt prompt = new Prompt(springAiMessages);
            String responseText = chatModel.call(prompt).getResult().getOutput().getText();
            
            System.out.println("LLM Response [Iteration " + currentIteration + "]:\n" + responseText);
            
            // Robust JSON extraction
            String jsonToParse = responseText;
            int firstBrace = jsonToParse.indexOf('{');
            int lastBrace = jsonToParse.lastIndexOf('}');
            if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
                jsonToParse = jsonToParse.substring(firstBrace, lastBrace + 1);
            }

            AgentAction action;
            try {
                action = outputConverter.convert(jsonToParse);
            } catch (Exception e) {
                System.out.println("LLM generated invalid JSON: " + e.getMessage());
                springAiMessages.add(new AssistantMessage(responseText));
                springAiMessages.add(new SystemMessage("Error parsing your JSON output. You must ONLY output a single valid JSON object."));
                currentIteration++;
                continue;
            }

            springAiMessages.add(new AssistantMessage(responseText));

            if (action.finalAnswer() != null && !action.finalAnswer().isBlank()) {
                return action.finalAnswer();
            }

            if (action.toolName() != null && !action.toolName().isBlank()) {
                ForgeTool tool = tools.get(action.toolName());
                if (tool == null) {
                    springAiMessages.add(new SystemMessage("Tool '" + action.toolName() + "' not found. Available tools: " + tools.keySet()));
                } else {
                    String jsonArgs = "{}";
                    try {
                        if (action.toolArguments() != null) {
                            jsonArgs = objectMapper.writeValueAsString(action.toolArguments());
                        }
                    } catch (JsonProcessingException e) {
                        // ignore, use "{}"
                    }
                    
                    ToolCall toolCall = new ToolCall(run.getId(), tool.getName(), jsonArgs);
                    toolCall = toolCallRepository.save(toolCall);
                    
                    long startTime = System.currentTimeMillis();
                    String result = tool.execute(jsonArgs);
                    long endTime = System.currentTimeMillis();
                    
                    toolCall.setResultText(result);
                    toolCall.setLatencyMs(endTime - startTime);
                    toolCall.setCompletedAt(Instant.now());
                    toolCallRepository.save(toolCall);
                    
                    springAiMessages.add(new SystemMessage("Tool '" + tool.getName() + "' executed successfully. Result:\n" + result));
                }
            } else {
                springAiMessages.add(new SystemMessage("You must either specify a 'toolName' to use a tool, or provide a 'finalAnswer'."));
            }

            currentIteration++;
        }

        return "Agent stopped after reaching maximum iterations (" + maxIterations + ") without providing a final answer.";
    }
}
