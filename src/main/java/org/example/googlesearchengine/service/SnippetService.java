package org.example.googlesearchengine.service;

import org.springframework.stereotype.Component;

@Component
public class SnippetService {

    private boolean containsQuery(String sentence, String query) {
        return sentence.toLowerCase().contains(query.toLowerCase());
    }

    public String generateSnippet(String content, String query) {
        if (content == null || content.isEmpty()) return "";

        String[] sentences = content.split("\\. ");

        for (String sentence : sentences) {
            if (containsQuery(sentence, query)) {
                return sentence.length() > 200
                        ? sentence.substring(0, 200) + "..."
                        : sentence;
            }
        }

        return content.length() > 200
                ? content.substring(0, 200) + "..."
                : content;
    }
}