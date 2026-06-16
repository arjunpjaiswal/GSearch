package org.example.googlesearchengine.controller;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.example.googlesearchengine.service.CrawlerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class CrawlerController {

    private final CrawlerService crawlerService;

    @PostMapping("/crawl")
    public ResponseEntity<String> startCrawl(@RequestBody CrawlRequest request) {

        if (request.getSeedUrls() == null || request.getSeedUrls().isEmpty()) {
            return ResponseEntity.badRequest().body("Seed URLs must not be empty");
        }

        log.info("Crawl request received — seeds: {}", request.getSeedUrls());

        crawlerService.startCrawl(request.getSeedUrls());

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body("Crawl started for " + request.getSeedUrls().size() + " seed URL(s)");
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CrawlRequest {
        private List<String> seedUrls;
    }
}