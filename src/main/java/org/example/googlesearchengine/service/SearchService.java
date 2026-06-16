package org.example.googlesearchengine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.googlesearchengine.dto.SearchResponse;
import org.example.googlesearchengine.dto.SearchResult;
import org.example.googlesearchengine.model.IndexEntry;
import org.example.googlesearchengine.repository.DocumentRepository;
import org.example.googlesearchengine.repository.IndexEntryRepository;
import org.example.googlesearchengine.util.Tokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    private final IndexEntryRepository indexEntryRepository;
    private final DocumentRepository documentRepository;
    private final Tokenizer tokenizer;
    private final AISummaryService aiSummaryService;

    @Value("${search.summary.top.results}")
    private int summaryTopResults;

    public SearchResponse search(String query, int page, int size) {

        // Step 1 — tokenize query
        List<String> queryTerms = tokenizer.tokenize(query);

        // Step 2 — accumulate scores per document across all query terms
        Map<Long, Double> scoreMap = new HashMap<>();
        for (String term : queryTerms) {
            for (IndexEntry entry : indexEntryRepository.findByTerm(term)) {
                Long documentId = entry.getDocument().getId();
                scoreMap.put(documentId,
                        scoreMap.getOrDefault(documentId, 0.0) + entry.getTfidfScore());
            }
        }

        // Step 3 — sort by combined score descending
        List<Map.Entry<Long, Double>> sortedEntries = new ArrayList<>(scoreMap.entrySet());
        sortedEntries.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Step 4 — paginate
        int fromIndex = page * size;
        int toIndex = Math.min(sortedEntries.size(), fromIndex + size);
        List<Map.Entry<Long, Double>> pageEntries =
                (fromIndex >= sortedEntries.size() || fromIndex > toIndex)
                        ? new ArrayList<>()
                        : sortedEntries.subList(fromIndex, toIndex);

        // Step 5 — build SearchResult only for this page
        List<SearchResult> results = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : pageEntries) {
            documentRepository.findById(entry.getKey()).ifPresent(doc ->
                    results.add(SearchResult.builder()
                            .title(doc.getTitle())
                            .url(doc.getUrl())
                            .snippet(doc.getSnippet())
                            .score(entry.getValue())
                            .build())
            );
        }

        // Step 6 — AI summary (wiring details pending)
        String aiSummary = aiSummaryService.generateSummary(query, results);

        // Step 7 — wrap in response
        return SearchResponse.builder()
                .query(query)
                .totalResults(sortedEntries.size())
                .currentPage(page)
                .pageSize(size)
                .results(results)
                .aiSummary(aiSummary)
                .build();
    }
}
