package org.example.googlesearchengine.repository;

import org.example.googlesearchengine.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByUrl(String url);
    Boolean existsByUrl(String url);
}