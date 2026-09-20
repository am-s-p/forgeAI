package com.forgeai.core.agent;

import java.util.Map;

public record AgentAction(
        String thoughtProcess,
        String toolName,
        Map<String, Object> toolArguments,
        String finalAnswer
) {}
