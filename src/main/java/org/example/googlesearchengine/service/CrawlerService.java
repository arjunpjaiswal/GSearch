package org.example.googlesearchengine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlerService {

    private final IndexerService indexerService;
    private final ExecutorService executorService;

    private final Set<String> visited = ConcurrentHashMap.newKeySet();
    private final AtomicInteger crawledCount = new AtomicInteger(0);

    @Value("${crawler.max.pages}")
    private int maxPages;

    @Value("${crawler.politeness.delay}")
    private int politenessDelay;

    public void startCrawl(List<String> seedUrls) {
        visited.clear();
        crawledCount.set(0);
        visited.addAll(seedUrls);

        for (String url : seedUrls) {
            executorService.submit(() -> crawlUrl(url));
        }
    }

    private void crawlUrl(String url) {
        if (crawledCount.get() >= maxPages) return;

        try {
            Thread.sleep(politenessDelay);

            org.jsoup.nodes.Document jsoupDoc = Jsoup.connect(url)
                    .userAgent("MiniSearchBot/1.0")
                    .timeout(5000)
                    .get();

            String title = jsoupDoc.title();
            String content = jsoupDoc.select("div#mw-content-text").text();

            indexerService.indexPage(url, title, content);
            crawledCount.incrementAndGet();
            log.info("Crawled [{}/{}]: {}", crawledCount.get(), maxPages, url);

            for (Element link : jsoupDoc.select("a[href]")) {
                String nextUrl = link.absUrl("href");
                if (shouldCrawl(nextUrl) && crawledCount.get() < maxPages) {
                    visited.add(nextUrl);
                    executorService.submit(() -> crawlUrl(nextUrl));
                }
            }

        } catch (Exception e) {
            log.error("Failed to crawl: {} → {}", url, e.getMessage());
        }
    }
    private boolean shouldCrawl(String url) {
        return url.startsWith("http")
                && !visited.contains(url)
                && !url.contains("#")
                && !url.endsWith(".pdf")
                && !url.endsWith(".jpg")
                && !url.endsWith(".png")
                && isEnglishWikipedia(url)
                // ADD THESE:
                && !url.contains("/Talk:")
                && !url.contains("/Template:")
                && !url.contains("/Template_talk:")
                && !url.contains("/Special:")
                && !url.contains("/Help:")
                && !url.contains("/Wikipedia:")
                && !url.contains("/File:")
                && !url.contains("/Category:")
                && !url.contains("/Portal:")
                && !url.contains("action=edit")
                && !url.contains("action=history")
                && !url.contains("action=info")
                && !url.contains("oldid=");
    }

    private boolean isEnglishWikipedia(String url) {
        // allow english wikipedia and non-wikipedia sites
        // block other language wikipedias like ka.wikipedia, th.wikipedia etc.
        if (url.contains("wikipedia.org")) {
            return url.contains("en.wikipedia.org");
        }
        return true;  // allow non-wikipedia URLs
    }

}