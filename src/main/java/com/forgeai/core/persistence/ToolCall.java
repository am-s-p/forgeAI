package com.forgeai.core.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tool_calls")
public class ToolCall {

    @Id
    private UUID id;

    @Column(name = "agent_run_id", nullable = false)
    private UUID agentRunId;

    @Column(name = "tool_name", nullable = false)
    private String toolName;

    @Column(name = "arguments_json", columnDefinition = "TEXT")
    private String argumentsJson;

    @Column(name = "result_text", columnDefinition = "TEXT")
    private String resultText;

    @Column(name = "error_text", columnDefinition = "TEXT")
    private String errorText;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected ToolCall() {}

    public ToolCall(UUID agentRunId, String toolName, String argumentsJson) {
        this.id = UUID.randomUUID();
        this.agentRunId = agentRunId;
        this.toolName = toolName;
        this.argumentsJson = argumentsJson;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getAgentRunId() { return agentRunId; }
    public String getToolName() { return toolName; }
    public String getArgumentsJson() { return argumentsJson; }
    public String getResultText() { return resultText; }
    public String getErrorText() { return errorText; }
    public Long getLatencyMs() { return latencyMs; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }

    public void setResultText(String resultText) { this.resultText = resultText; }
    public void setErrorText(String errorText) { this.errorText = errorText; }
    public void setLatencyMs(Long latencyMs) { this.latencyMs = latencyMs; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
