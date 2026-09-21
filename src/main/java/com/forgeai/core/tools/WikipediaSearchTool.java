package com.forgeai.core.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
public class WikipediaSearchTool implements ForgeTool {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return "wikipedia_search";
    }

    @Override
    public String getDescription() {
        return "Search Wikipedia for factual information about people, places, historical events, or concepts. Provide a 'query' to search for. ONLY use this when you need external facts not in your document knowledge base. Example input: {\"query\": \"Albert Einstein\"}";
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

            // Wikipedia API: query with generator=search
            String url = UriComponentsBuilder.fromHttpUrl("https://en.wikipedia.org/w/api.php")
                    .queryParam("action", "query")
                    .queryParam("format", "json")
                    .queryParam("prop", "extracts")
                    .queryParam("exintro", "true")
                    .queryParam("explaintext", "true")
                    .queryParam("generator", "search")
                    .queryParam("gsrsearch", query)
                    .queryParam("gsrlimit", "1")
                    .build().toUriString();

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode pages = root.path("query").path("pages");
            
            if (pages.isMissingNode() || pages.isEmpty()) {
                return "No Wikipedia article found for query: " + query;
            }

            // Get the first page
            JsonNode firstPage = pages.elements().next();
            JsonNode extractNode = firstPage.path("extract");
            
            if (extractNode.isMissingNode() || extractNode.asText().isBlank()) {
                return "No summary available on Wikipedia for query: " + query;
            }

            return "Wikipedia Summary: " + extractNode.asText();

        } catch (Exception e) {
            return "Error executing Wikipedia search: " + e.getMessage();
        }
    }
}
