package com.forgeai.core.api;

import com.forgeai.core.agent.PlanResponse;
import java.util.UUID;

public record ChatResponse(
        UUID conversationId,
        PlanResponse plan
) {}
