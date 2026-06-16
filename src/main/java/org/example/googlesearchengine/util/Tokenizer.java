package org.example.googlesearchengine.util;

import org.springframework.stereotype.Component;

import java.util.*;
@Component
public class Tokenizer {

    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "is", "it", "in", "on", "at",
            "to", "and", "or", "but", "are", "was", "for",
            "of", "with", "this", "that", "be", "as", "by",
            "its", "not", "from", "has", "have", "had"
    ));

    public List<String> tokenize(String text) {
        if (text == null || text.isEmpty()) return new ArrayList<>();

        String lower = text.toLowerCase();
        String[] words = lower.split("[^a-zA-Z0-9]+");

        List<String> result = new ArrayList<>();
        for (String word : words) {
            if (!word.isEmpty() && !STOPWORDS.contains(word)) {
                result.add(word);
            }
        }
        return result;
    }
}