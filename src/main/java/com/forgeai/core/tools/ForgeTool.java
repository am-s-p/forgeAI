package com.forgeai.core.tools;

public interface ForgeTool {
    String getName();
    String getDescription();
    String execute(String jsonArgs);
}
