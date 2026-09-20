package com.forgeai.core.agent;

import java.util.List;

public record PlanResponse(
        String thoughtProcess,
        List<String> actionSteps
) {}
