# GSearch — Mini Search Engine

A search engine built from scratch in Spring Boot — a multithreaded BFS web crawler, a custom inverted index with TF-IDF ranking (no Elasticsearch/Lucene), a paginated REST search API, and AI-generated result summaries powered by Groq — similar in spirit to Google's AI Overviews.

Built as a deep-dive learning project to understand how search engines actually work under the hood, rather than configuring an existing search platform.

---

## Live Demo

🔗 **[https://gsearch-19c9.onrender.com](https://gsearch-19c9.onrender.com)**

> First load may take 30-60 seconds if the server has been idle (free tier). Search for `java`, `algorithm`, `machine learning`, or `data structure`.

---

## Features

- **Multithreaded BFS web crawler** — Jsoup-based, 10-thread `ExecutorService` pool, politeness delay, duplicate-URL detection via `ConcurrentHashMap`
- **Inverted index** — custom-built, stored in MySQL, term → document mapping with B-tree indexing
- **TF-IDF ranking** — relevance scoring calculated at crawl time, no external search library
- **Paginated REST search API** — multi-word query support with additive score ranking
- **AI-generated summaries** — Groq generates a short overview from top search results (RAG-style: retrieval-augmented, grounded in indexed content only — no hallucination)
- **Google-style frontend** — single HTML file, voice search, favicons, AI Overview box, pagination, XSS protection
- **Auto re-crawl** — scheduled background re-crawling at 2 AM daily to keep the index fresh

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3, Java 17 |
| Database | MySQL 8 (Spring Data JPA / Hibernate) |
| Crawling | Jsoup |
| AI Summary | Groq API (`openai/gpt-oss-20b`) via `RestTemplate` |
| Frontend | Plain HTML, CSS, JavaScript (no framework) |
| Build | Gradle |
| Hosting | Render (app) + Aiven (MySQL) |

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
   3. Accumulate TF-IDF scores per document across all terms
   4. Sort by combined score descending, paginate via subList()
   5. Fetch full Document rows only for the current page (FetchType.LAZY)
   6. Send top 5 results to AISummaryService → Groq → AI summary
        │
        ▼
 SearchResponse (JSON) → rendered by index.html
```

```
CrawlerService  (BFS, 10 threads via ExecutorService)
   → fetches page with Jsoup (Wikipedia article content only)
   → IndexerService tokenizes content, calculates TF-IDF
   → saves Document + IndexEntry rows atomically (@Transactional)
   → discovers links, self-submits new tasks to thread pool
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
- A free Groq API key — [console.groq.com](https://console.groq.com) (free tier: ~1,200 searches/day)

### 1. Clone and configure

```bash
git clone https://github.com/arjunpjaiswal/GSearch.git
cd GSearch
```

### 2. Set environment variables

This project reads secrets from environment variables — **no credentials are stored in the repository**.

```bash
export DB_URL=jdbc:mysql://localhost:3306/searchengine?createDatabaseIfNotExist=true
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password
export GROQ_API_KEY=your_groq_api_key
```

In IntelliJ: **Run/Debug Configurations → Modify options → Environment variables**
```
DB_URL=jdbc:mysql://localhost:3306/searchengine?createDatabaseIfNotExist=true;DB_USERNAME=root;DB_PASSWORD=your_password;GROQ_API_KEY=your_key
```

### 3. Create the database

```sql
CREATE DATABASE searchengine;
```

### 4. Run

```bash
./gradlew bootRun
```

On first launch the crawler automatically indexes ~500 Wikipedia pages (takes 1-3 minutes). Watch the console — once you see crawl logs, search is ready. Subsequent launches skip re-crawling if data already exists.

### 5. Search

Open `http://localhost:8080` and type a query — `java`, `algorithm`, `machine learning` etc.

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

Returns `202 Accepted` immediately — crawling runs asynchronously in the background.

---

## API Reference

**`GET /api/search`**

| Param | Type | Required | Default |
|---|---|---|---|
| `query` | string | yes | — |
| `page` | int | no | `0` |
| `size` | int | no | `10` |

```json
{
  "query": "java",
  "aiSummary": "Java is a high-level, class-based, object-oriented programming language...",
  "totalResults": 137,
  "currentPage": 0,
  "pageSize": 10,
  "results": [
    {
      "title": "Java (programming language)",
      "url": "https://en.wikipedia.org/wiki/Java_(programming_language)",
      "snippet": "Java is a high-level, class-based, object-oriented programming language...",
      "score": 4.82
    }
  ]
}
```

**`POST /api/crawl`**

```json
{ "seedUrls": ["https://en.wikipedia.org/wiki/..."] }
```

Returns `202 Accepted` immediately — crawling continues asynchronously.

---

## Configuration

All tunable values live in `application.properties`:

```properties
crawler.max.pages=500
crawler.politeness.delay=1000
crawler.thread.pool.size=10
search.default.page.size=10
search.summary.top.results=5
groq.model=openai/gpt-oss-20b
groq.max.tokens=400
```

---

## How It Works

**Inverted Index** — every crawled page is tokenized into words. Each word maps to the documents containing it (`term → [doc1, doc7, doc23...]`). Search becomes a direct O(log N) lookup via B-tree index instead of scanning all pages.

**TF-IDF Ranking** — each word gets a relevance score: how often it appears in this document (TF) × how rare it is across all documents (IDF). Pre-calculated at crawl time using `@Transactional` atomicity, fetched instantly at search time.

**RAG AI Summary** — top 5 search results are sent to Groq as context. The model generates a summary grounded **only** in those results — constrained by system prompt to prevent hallucination.

---

## Known Limitations

This is an MVP built to demonstrate core search engine mechanics, not a production system:

- **No stemming** — "run" and "running" are indexed as different terms
- **No phrase search** — `"spring boot"` is not treated as an exact phrase
- **No robots.txt compliance** — crawler does not check/respect `robots.txt`
- **OFFSET-based pagination** — degrades at very deep pages (cursor-based pagination would fix this at scale)
- **TF-IDF scores drift slightly stale** between crawls as corpus grows
- **No JavaScript rendering** — Jsoup cannot crawl React/Vue/Angular pages; works on static HTML (Wikipedia, Baeldung etc.)

## Possible Next Steps (V2)

- Replace MySQL inverted index with **Elasticsearch** (BM25 ranking, fuzzy search, horizontal scaling)
- **Trie-based autocomplete** from indexed terms
- **Redis caching** for frequent queries
- **Distributed crawling** (Kafka URL frontier, Redis visited set)
- **robots.txt** parsing and compliance
- **PageRank**-style link authority scoring combined with TF-IDF
- **Stemming** via Porter Stemmer

---

## Author

**Arjun Jaiswal**
B.Tech Information Technology — RCOEM (Ramdeobaba University), Nagpur — 2027

[GitHub](https://github.com/arjunpjaiswal) · [LinkedIn](https://www.linkedin.com/in/arjun-pankaj-jaiswal-b297752a2/)