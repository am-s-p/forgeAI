package com.forgeai.core.persistence;

import org.springframework.data.repository.CrudRepository;
import java.util.UUID;

public interface ToolCallRepository extends CrudRepository<ToolCall, UUID> {}
