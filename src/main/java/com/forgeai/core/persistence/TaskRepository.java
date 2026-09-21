package com.forgeai.core.persistence;

import org.springframework.data.repository.CrudRepository;
import java.util.UUID;

import java.util.List;

public interface TaskRepository extends CrudRepository<Task, UUID> {
    List<Task> findByConversationId(UUID conversationId);
}
