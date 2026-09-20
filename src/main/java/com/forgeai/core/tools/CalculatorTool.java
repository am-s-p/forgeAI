package com.forgeai.core.tools;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class CalculatorTool implements ForgeTool {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CalculatorTool() {
    }

    @Override
    public String getName() {
        return "calculatorTool";
    }

    @Override
    public String getDescription() {
        return "A calculator that can add, subtract, multiply, or divide two numbers. Useful for math operations. Arguments should be JSON: {\"a\": int, \"b\": int, \"operation\": \"add|subtract|multiply|divide\"}";
    }

    public record Request(int a, int b, String operation) {}

    @Override
    public String execute(String jsonArgs) {
        try {
            Request request = objectMapper.readValue(jsonArgs, Request.class);
            int result = switch (request.operation().toLowerCase()) {
                case "add", "addition", "+" -> request.a() + request.b();
                case "subtract", "subtraction", "-" -> request.a() - request.b();
                case "multiply", "multiplication", "*" -> request.a() * request.b();
                case "divide", "division", "/" -> {
                    if (request.b() == 0) throw new IllegalArgumentException("Cannot divide by zero");
                    yield request.a() / request.b();
                }
                default -> throw new IllegalArgumentException("Unknown operation: " + request.operation());
            };
            return String.valueOf(result);
        } catch (JsonProcessingException e) {
            return "Error parsing arguments: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (Exception e) {
            return "Unexpected error: " + e.getMessage();
        }
    }
}
