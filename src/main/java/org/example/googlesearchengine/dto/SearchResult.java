package org.example.googlesearchengine.dto;

import lombok.*;

@Getter
@Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SearchResult {
    private String title;
    private String url;
    private String snippet;
    private double score;
}
