package com.forgeai.core.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgeai.core.persistence.AgentRun;
import com.forgeai.core.persistence.Conversation;
import com.forgeai.core.persistence.Message;
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

@Service
public class AgentOrchestrator {

    private final ChatModel chatModel;
    private final ToolCallRepository toolCallRepository;
    private final Map<String, ForgeTool> tools;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BeanOutputConverter<AgentAction> outputConverter;

    public AgentOrchestrator(ChatModel chatModel, ToolCallRepository toolCallRepository, List<ForgeTool> toolList) {
        this.chatModel = chatModel;
        this.toolCallRepository = toolCallRepository;
        this.tools = toolList.stream().collect(Collectors.toMap(ForgeTool::getName, t -> t));
        this.outputConverter = new BeanOutputConverter<>(AgentAction.class);
    }

    public String run(Conversation conversation, AgentRun run) {
        String toolsSchema = tools.values().stream()
                .map(t -> "- " + t.getName() + ": " + t.getDescription())
                .collect(Collectors.joining("\n"));

        String format = outputConverter.getFormat();
        String systemPromptText = """
                You are ForgeAI, an advanced agent capable of multi-step reasoning and tool use.
                Answer the user's request, considering the conversation history.
                
                You have access to the following tools:
                %s
                
                FORMAT INSTRUCTIONS:
                You must follow the format strictly. If you want to use a tool, provide 'toolName' and 'toolArguments'.
                If you have reached the final answer and no more tools are needed, provide 'finalAnswer'.
                %s
                """.formatted(toolsSchema, format);

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

        int maxIterations = 10;
        int currentIteration = 0;

        while (currentIteration < maxIterations) {
            Prompt prompt = new Prompt(springAiMessages);
            String responseText = chatModel.call(prompt).getResult().getOutput().getText();
            
            AgentAction action;
            try {
                action = outputConverter.convert(responseText);
            } catch (Exception e) {
                // If the LLM failed to format correctly, tell it to try again
                springAiMessages.add(new AssistantMessage(responseText));
                springAiMessages.add(new SystemMessage("Error parsing your JSON output. Please strictly follow the requested JSON schema."));
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

        return "Agent stopped after reaching maximum iterations without providing a final answer.";
    }
}
