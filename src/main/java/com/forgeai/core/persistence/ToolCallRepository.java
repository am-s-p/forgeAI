package com.forgeai.core.persistence;

import org.springframework.data.repository.CrudRepository;
import java.util.UUID;

import java.util.List;

public interface ToolCallRepository extends CrudRepository<ToolCall, UUID> {
    List<ToolCall> findByAgentRunId(UUID agentRunId);
}
