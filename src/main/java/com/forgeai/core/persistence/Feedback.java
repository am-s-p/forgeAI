package com.forgeai.core.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    private UUID id;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Column(name = "is_positive", nullable = false)
    private Boolean isPositive;

    @Column(name = "correction_text", columnDefinition = "TEXT")
    private String correctionText;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Feedback() {}

    public Feedback(UUID conversationId, Boolean isPositive, String correctionText) {
        this.id = UUID.randomUUID();
        this.conversationId = conversationId;
        this.isPositive = isPositive;
        this.correctionText = correctionText;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getConversationId() { return conversationId; }
    public Boolean getIsPositive() { return isPositive; }
    public String getCorrectionText() { return correctionText; }
    public Instant getCreatedAt() { return createdAt; }
}
