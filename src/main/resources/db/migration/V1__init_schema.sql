CREATE TABLE conversations (
    id UUID PRIMARY KEY,
    title VARCHAR(255),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_conversation
        FOREIGN KEY(conversation_id) 
        REFERENCES conversations(id)
        ON DELETE CASCADE
);
