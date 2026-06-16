package org.example.googlesearchengine.controller;
import org.springframework.beans.factory.annotation.Value; // CORRECT
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.example.googlesearchengine.dto.SearchResponse;
import org.example.googlesearchengine.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchService searchService;

    @Value("${search.default.page.size}")
    private int defaultPageSize;

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size) {

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        int pageSize = (size != null) ? size : defaultPageSize;

        log.info("Search request — query: '{}', page: {}, size: {}", query, page, pageSize);

        SearchResponse response = searchService.search(query.trim(), page, pageSize);
        return ResponseEntity.ok(response);
    }
}
