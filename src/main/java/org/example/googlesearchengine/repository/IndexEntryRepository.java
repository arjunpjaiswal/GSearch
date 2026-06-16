package org.example.googlesearchengine.repository;

import org.example.googlesearchengine.model.Document;
import org.example.googlesearchengine.model.IndexEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndexEntryRepository extends JpaRepository<IndexEntry, Long> {
    List<IndexEntry> findByTerm(String term);
    List<IndexEntry> findByTermOrderByTfidfScoreDesc(String term, Pageable pageable);
    void deleteByDocument(Document document);
    long countByTerm(String term);
}