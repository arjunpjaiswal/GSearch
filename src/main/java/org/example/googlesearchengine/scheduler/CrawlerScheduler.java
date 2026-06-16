package org.example.googlesearchengine.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.googlesearchengine.repository.DocumentRepository;
import org.example.googlesearchengine.service.CrawlerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CrawlerScheduler {

    private final CrawlerService crawlerService;
    private final DocumentRepository documentRepository;

    @Value("${crawler.seed.urls}")
    private String seedUrlsRaw;

    // runs on every startup — skips if DB already has data
    @EventListener(ApplicationReadyEvent.class)
    public void crawlOnStartup() {
        if (documentRepository.count() > 0) {
            log.info("Database already has {} documents, skipping startup crawl",
                    documentRepository.count());
            return;
        }

        log.info("Empty database detected — starting initial crawl");
        triggerCrawl();
    }

    // runs every 24 hours regardless — always refreshes content
    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledCrawl() {
        log.info("Scheduled crawl starting");
        triggerCrawl();
        log.info("Scheduled crawl triggered successfully");
    }

    private void triggerCrawl() {
        List<String> seedUrls = Arrays.stream(seedUrlsRaw.split(","))
                .map(String::trim)
                .filter(url -> !url.isEmpty())
                .collect(Collectors.toList());

        if (seedUrls.isEmpty()) {
            log.warn("Crawl skipped — no seed URLs configured");
            return;
        }

        log.info("Crawl starting — seeds: {}", seedUrls);
        crawlerService.startCrawl(seedUrls);
    }
}