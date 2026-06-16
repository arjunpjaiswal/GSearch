package org.example.googlesearchengine.model;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 2048)
    private String url;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String snippet;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "crawled_at")
    private LocalDateTime crawledAt;
}