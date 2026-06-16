package org.example.googlesearchengine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.googlesearchengine.model.Document;
import org.example.googlesearchengine.model.IndexEntry;
import org.example.googlesearchengine.repository.DocumentRepository;
import org.example.googlesearchengine.repository.IndexEntryRepository;
import org.example.googlesearchengine.util.TfIdfCalculator;
import org.example.googlesearchengine.util.Tokenizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;  // ← ADD THIS

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexerService {

    private final DocumentRepository documentRepository;
    private final IndexEntryRepository indexEntryRepository;
    private final Tokenizer tokenizer;
    private final TfIdfCalculator tfIdfCalculator;
    private final SnippetService snippetService;

    @Transactional  // ← ADD THIS
    public void indexPage(String url, String title, String content) {

        Optional<Document> existing = documentRepository.findByUrl(url);
        Document document;

        if (existing.isPresent()) {
            document = existing.get();
            indexEntryRepository.deleteByDocument(document);
        } else {
            document = Document.builder()
                    .url(url)
                    .title(title)
                    .snippet(snippetService.generateSnippet(content, ""))
                    .content(content)
                    .crawledAt(LocalDateTime.now())
                    .build();
        }

        document.setContent(content);
        document.setTitle(title);
        document.setCrawledAt(LocalDateTime.now());
        document = documentRepository.save(document);

        List<String> tokens = tokenizer.tokenize(content);
        if (tokens.isEmpty()) return;

        Map<String, Integer> frequencyMap = new HashMap<>();
        for (String token : tokens) {
            frequencyMap.put(token, frequencyMap.getOrDefault(token, 0) + 1);
        }

        int totalWords = tokens.size();
        long totalDocuments = documentRepository.count();

        List<IndexEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
            String term = entry.getKey();
            int termFreq = entry.getValue();

            long docsContaining = indexEntryRepository.countByTerm(term) + 1;

            double score = tfIdfCalculator.calculateTfIdf(
                    termFreq, totalWords, totalDocuments, docsContaining
            );

            entries.add(IndexEntry.builder()
                    .term(term)
                    .document(document)
                    .termFreq((long) termFreq)
                    .tfidfScore(score)
                    .build());
        }

        indexEntryRepository.saveAll(entries);
        log.info("Indexed {} terms for: {}", entries.size(), url);
    }
}