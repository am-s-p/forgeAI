package com.forgeai.core.persistence;

import org.springframework.data.repository.CrudRepository;
import java.util.UUID;

import java.util.List;

public interface AgentRunRepository extends CrudRepository<AgentRun, UUID> {
    List<AgentRun> findByTaskId(UUID taskId);
}
