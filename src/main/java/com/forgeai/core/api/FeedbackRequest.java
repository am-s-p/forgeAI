package com.forgeai.core.api;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record FeedbackRequest(
        @NotNull(message = "Conversation ID is required")
        UUID conversationId,
        @NotNull(message = "isPositive flag is required")
        Boolean isPositive,
        String correctionText
) {}
