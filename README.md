# Mini Search Engine

A search engine built from scratch in Spring Boot — a multithreaded BFS web crawler, a custom inverted index with TF-IDF ranking (no Elasticsearch/Lucene), a paginated REST search API, and AI-generated result summaries powered by Groq — similar in spirit to Google's AI Overviews.

Built as a deep-dive learning project to understand how search engines actually work under the hood, rather than configuring an existing search platform.

---
## Live Demo

🔗 [https://gsearch-19c9.onrender.com/](https://gsearch-19c9.onrender.com/)
## Features

- **Multithreaded BFS web crawler** — Jsoup-based, 10-thread `ExecutorService` pool, politeness delay, duplicate-URL detection
- **Inverted index** — custom-built, stored in MySQL, term → document mapping
- **TF-IDF ranking** — relevance scoring calculated at crawl time, no external search library
- **Paginated REST search API** — multi-word query support with additive score ranking
- **AI-generated summaries** — Groq (Llama 3.1 8B) generates a short overview from top search results (RAG-style: retrieval-augmented, grounded in indexed content only)
- **Google-style frontend** — single HTML file, voice search, favicons, AI Overview box, pagination
- **Auto re-crawl** — scheduled background re-crawling to keep the index fresh

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3, Java 17 |
| Database | MySQL 8 (Spring Data JPA / Hibernate) |
| Crawling | Jsoup |
| AI Summary | Groq API (`llama-3.1-8b-instant`) via `RestTemplate` |
| Frontend | Plain HTML, CSS, JavaScript (no framework) |
| Build | Gradle |

---

## Architecture

```
User searches "spring boot"
        │
        ▼
 SearchController  (GET /api/search)
        │
        ▼
 SearchService
   1. Tokenize query → ["spring", "boot"]
   2. Look up each term in index_entries (inverted index)
   3. Accumulate TF-IDF scores per document
   4. Sort by combined score, paginate
   5. Fetch full Document rows only for the current page
   6. Send top 5 results to AISummaryService → Groq → AI summary
        │
        ▼
 SearchResponse (JSON) → rendered by index.html
```

```
CrawlerService  (BFS, 10 threads via ExecutorService)
   → fetches page with Jsoup
   → IndexerService tokenizes content, calculates TF-IDF
   → saves Document + IndexEntry rows (MySQL)
   → discovers links, submits new crawl tasks
```

**Core tables**

| Table | Purpose |
|---|---|
| `documents` | One row per crawled page — url, title, snippet, content, crawled_at |
| `index_entries` | One row per unique word per document — term, document_id, term_freq, tfidf_score |

---

## Getting Started

### Prerequisites

- Java 17+
- MySQL 8 running locally
- A free Groq API key — [console.groq.com](https://console.groq.com)

### 1. Clone and configure

```bash
git clone https://github.com/arjunpjaiswal/Gsearch.git
cd Gsearch
```

### 2. Set environment variables

This project reads secrets from environment variables — **no credentials are stored in the repository**.

```bash
export DB_PASSWORD=your_mysql_password
export GROQ_API_KEY=your_groq_api_key
```

Or, in IntelliJ: **Run/Debug Configurations → Modify options → Environment variables**
```
DB_PASSWORD=your_mysql_password;GROQ_API_KEY=your_groq_api_key
```

### 3. Create the database

```sql
CREATE DATABASE searchengine;
```
(Or let Hibernate create it automatically — `createDatabaseIfNotExist=true` is already set.)

### 4. Run

```bash
./gradlew bootRun
```

The crawler starts automatically on launch using the seed URLs configured in `application.properties` (currently a set of Wikipedia CS/programming articles). Crawling ~500 pages takes roughly 1–3 minutes.

### 5. Search

Open:
```
http://localhost:8080
```

Type a query (e.g. `java`, `algorithm`, `spring boot`) once the console shows pages being crawled.

### Trigger a crawl manually (optional)

```http
POST http://localhost:8080/api/crawl
Content-Type: application/json

{
  "seedUrls": [
    "https://en.wikipedia.org/wiki/Java_(programming_language)",
    "https://en.wikipedia.org/wiki/Algorithm"
  ]
}
```

---

## API Reference

**`GET /api/search`**

| Param | Type | Required | Default |
|---|---|---|---|
| `query` | string | yes | — |
| `page` | int | no | `0` |
| `size` | int | no | `search.default.page.size` (10) |

```json
{
  "query": "java",
  "aiSummary": "Java is a high-level, class-based, object-oriented programming language...",
  "totalResults": 137,
  "currentPage": 0,
  "pageSize": 10,
  "results": [
    { "title": "Java (programming language)", "url": "https://en.wikipedia.org/wiki/Java_(programming_language)", "snippet": "...", "score": 4.82 }
  ]
}
```

**`POST /api/crawl`**

```json
{ "seedUrls": ["https://en.wikipedia.org/wiki/..."] }
```
Returns `202 Accepted` immediately — crawling continues asynchronously in the background.

---

## Configuration

All tunable values live in `application.properties`:

```properties
crawler.max.pages=500
crawler.politeness.delay=1000
crawler.thread.pool.size=10
search.default.page.size=10
search.summary.top.results=5
groq.model=llama-3.1-8b-instant
```

---

## Known Limitations

This is an MVP built to demonstrate core search engine mechanics, not a production system. Known gaps:

- **No stemming** — "run" and "running" are indexed as different terms
- **No phrase search** — `"spring boot"` is not treated as an exact phrase
- **No robots.txt compliance** — crawler does not currently check/respect `robots.txt`
- **OFFSET-based pagination** — degrades at very deep pages on large result sets (would move to cursor-based pagination at scale)
- **TF-IDF scores can drift slightly stale** between crawls as the corpus grows (IDF changes as `totalDocuments` changes)
- **No JavaScript rendering** — Jsoup cannot crawl client-rendered (React/Vue/Angular) pages; works correctly on static HTML sites like Wikipedia

## Possible Next Steps

- Replace MySQL inverted index with Elasticsearch (BM25 ranking, fuzzy search, scale)
- Trie-based autocomplete from indexed terms
- Redis caching for frequent queries
- Distributed crawling (Kafka-backed URL frontier, Redis-backed visited set)
- robots.txt parsing and politeness compliance
- PageRank-style link authority scoring combined with TF-IDF

---

## Author

Arjun Jaiswal — B.Tech Information Technology, RCOEM, Nagpur (2027)
