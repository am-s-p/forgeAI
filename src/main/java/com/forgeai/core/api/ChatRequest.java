package com.forgeai.core.api;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record ChatRequest(
        UUID conversationId,
        @NotBlank(message = "Message cannot be empty")
        String message
) {}
