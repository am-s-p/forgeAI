CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    CONSTRAINT fk_task_conversation
        FOREIGN KEY(conversation_id) 
        REFERENCES conversations(id)
        ON DELETE CASCADE
);

CREATE TABLE agent_runs (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL,
    plan_json TEXT,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    CONSTRAINT fk_run_task
        FOREIGN KEY(task_id) 
        REFERENCES tasks(id)
        ON DELETE CASCADE
);

CREATE TABLE tool_calls (
    id UUID PRIMARY KEY,
    agent_run_id UUID NOT NULL,
    tool_name VARCHAR(100) NOT NULL,
    arguments_json TEXT,
    result_text TEXT,
    error_text TEXT,
    latency_ms BIGINT,
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    CONSTRAINT fk_tool_run
        FOREIGN KEY(agent_run_id) 
        REFERENCES agent_runs(id)
        ON DELETE CASCADE
);
