package com.forgeai.core.api;

import com.forgeai.core.agent.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:5173")
public class ChatController {

    private final ChatService chatService;
    private final com.forgeai.core.persistence.FeedbackRepository feedbackRepository;

    public ChatController(ChatService chatService, com.forgeai.core.persistence.FeedbackRepository feedbackRepository) {
        this.chatService = chatService;
        this.feedbackRepository = feedbackRepository;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatService.chat(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/feedback")
    public ResponseEntity<Void> submitFeedback(@Valid @RequestBody FeedbackRequest request) {
        com.forgeai.core.persistence.Feedback feedback = new com.forgeai.core.persistence.Feedback(
                request.conversationId(),
                request.isPositive(),
                request.correctionText()
        );
        feedbackRepository.save(feedback);
        return ResponseEntity.ok().build();
    }
}
