package org.example.googlesearchengine.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "index_entries",
        indexes = {
                @Index(name = "idx_term", columnList = "term"),
                @Index(name = "idx_document_id", columnList = "document_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"term", "document_id"})
        }
)
public class IndexEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String term;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(nullable = false)
    private Long termFreq;

    @Column(nullable = false)
    private Double tfidfScore;
}