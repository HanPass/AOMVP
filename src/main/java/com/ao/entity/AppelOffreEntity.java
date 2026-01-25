package com.ao.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "appel_offre",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_appel_offre_reference", columnNames = "reference")
        },
        indexes = {
                @Index(name = "idx_appel_offre_created_at", columnList = "created_at"),
                @Index(name = "idx_appel_offre_date_limite", columnList = "date_limite")
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppelOffreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Clé métier (unique)
    @Column(name = "reference", nullable = false, length = 100)
    private String reference;

    @Column(name = "organisme", length = 255)
    private String organisme;

    @Column(name = "lieu_exec", length = 255)
    private String lieuExec;

    @Column(name = "objet", columnDefinition = "TEXT")
    private String objet;

    @Column(name = "date_publication")
    private LocalDate datePublication;

    @Column(name = "date_limite")
    private LocalDateTime dateLimite;

    @Column(name = "url_detail", columnDefinition = "TEXT")
    private String urlDetail;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
