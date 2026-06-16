package org.example.googlesearchengine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.googlesearchengine.dto.SearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AISummaryService {

    private final RestTemplate restTemplate;

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.api.url}")
    private String groqApiUrl;

    @Value("${groq.model}")
    private String groqModel;

    @Value("${groq.max.tokens}")
    private int maxTokens;

    private static final String SYSTEM_PROMPT =
            "You are a search engine summarizer. Based ONLY on the provided search results, " +
                    "write a 3-4 sentence summary answering the user's query. " +
                    "If the results do not contain relevant information, respond with " +
                    "'I could not find enough information about this topic.' " +
                    "Do not use any outside knowledge.";

    public String generateSummary(String query, List<SearchResult> results) {
        if (results == null || results.isEmpty()) return null;

        StringBuilder context = new StringBuilder();
        int i = 1;
        for (SearchResult result : results) {
            context.append(i).append(". ")
                    .append(result.getTitle()).append(": ")
                    .append(result.getSnippet()).append("\n");
            i++;
        }

        String userMessage = "Query: " + query + "\n\nSearch results:\n" + context;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", groqModel);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", userMessage)
        ));

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + groqApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(groqApiUrl, requestEntity, Map.class);

            Map<String, Object> body = response.getBody();
            List<Map> choices = (List<Map>) body.get("choices");
            Map message = (Map) choices.get(0).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            log.warn("Groq API call failed, skipping AI summary: {}", e.getMessage());
            return null;
        }
    }
}