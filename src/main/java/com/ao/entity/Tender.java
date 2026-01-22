package com.ao.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tender",
        uniqueConstraints = @UniqueConstraint(columnNames = "sourceId")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tender {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sourceId;

    private String reference;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String organisme;

    private String region;

    private String deadline;

    @Column(columnDefinition = "TEXT")
    private String url;

    private LocalDateTime createdAt = LocalDateTime.now();
}

