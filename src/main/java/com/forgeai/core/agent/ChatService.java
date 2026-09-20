package com.forgeai.core.agent;

import com.forgeai.core.api.ChatRequest;
import com.forgeai.core.api.ChatResponse;
import com.forgeai.core.persistence.Conversation;
import com.forgeai.core.persistence.ConversationRepository;
import com.forgeai.core.persistence.Message;
import com.forgeai.core.persistence.Task;
import com.forgeai.core.persistence.TaskRepository;
import com.forgeai.core.persistence.AgentRun;
import com.forgeai.core.persistence.AgentRunRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final TaskRepository taskRepository;
    private final AgentRunRepository agentRunRepository;
    private final AgentOrchestrator agentOrchestrator;

    public ChatService(ConversationRepository conversationRepository, TaskRepository taskRepository, AgentRunRepository agentRunRepository, AgentOrchestrator agentOrchestrator) {
        this.conversationRepository = conversationRepository;
        this.taskRepository = taskRepository;
        this.agentRunRepository = agentRunRepository;
        this.agentOrchestrator = agentOrchestrator;
    }

    public ChatResponse chat(ChatRequest request) {
        Conversation conversation;
        if (request.conversationId() != null) {
            conversation = conversationRepository.findById(request.conversationId())
                    .orElseGet(() -> new Conversation("Chat - " + request.message().substring(0, Math.min(request.message().length(), 20))));
        } else {
            conversation = new Conversation("Chat - " + request.message().substring(0, Math.min(request.message().length(), 20)));
        }
        
        conversation.addMessage(new Message("USER", request.message()));
        conversation = conversationRepository.save(conversation);

        Task task = new Task(conversation.getId(), request.message());
        task = taskRepository.save(task);

        AgentRun run = new AgentRun(task.getId());
        run = agentRunRepository.save(run);

        // Run the agent loop
        String finalAnswer = agentOrchestrator.run(conversation, run);
        
        conversation.addMessage(new Message("ASSISTANT", finalAnswer));
        conversationRepository.save(conversation);

        run.setStatus("COMPLETED");
        run.setCompletedAt(Instant.now());
        agentRunRepository.save(run);

        task.setStatus("COMPLETED");
        task.setCompletedAt(Instant.now());
        taskRepository.save(task);
        
        PlanResponse plan = new PlanResponse("Agent reasoning concluded.", java.util.List.of(finalAnswer));
        return new ChatResponse(conversation.getId(), plan);
    }
}
