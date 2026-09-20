package com.forgeai.core.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DocumentRetrievalTool implements ForgeTool {

    private final SimpleVectorStore vectorStore;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DocumentRetrievalTool(SimpleVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public String getName() {
        return "document_retrieval";
    }

    @Override
    public String getDescription() {
        return "Search the local vector database for information from ingested documents. Provide a 'query' to search for. ONLY use this when you need external knowledge. Example input: {\"query\": \"what is the return policy?\"}";
    }

    @Override
    public String execute(String jsonArgs) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> args = objectMapper.readValue(jsonArgs, Map.class);
            String query = args.get("query");
            if (query == null || query.isBlank()) {
                return "Error: Missing 'query' argument.";
            }

            List<Document> results = vectorStore.similaritySearch(SearchRequest.builder().query(query).topK(3).build());
            
            if (results.isEmpty()) {
                return "No relevant documents found for the query.";
            }

            return results.stream()
                    .map(doc -> "Document Content: " + doc.getText())
                    .collect(Collectors.joining("\n\n---\n\n"));
        } catch (JsonProcessingException e) {
            return "Error parsing arguments: " + e.getMessage();
        }
    }
}
