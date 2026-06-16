package org.example.googlesearchengine.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SearchResponse {
    private String query;
    private String aiSummary;
    private long totalResults;
    private int currentPage;
    private int pageSize;
    private List<SearchResult> results;
}