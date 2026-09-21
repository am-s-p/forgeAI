package com.forgeai.core.persistence;

import org.springframework.data.repository.CrudRepository;
import java.util.UUID;
import java.util.List;

public interface FeedbackRepository extends CrudRepository<Feedback, UUID> {
    List<Feedback> findByConversationId(UUID conversationId);
}
