package com.ao.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_preference")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;

    @Column(name = "regions", columnDefinition = "TEXT")
    private String regions;

    @Column(name = "organismes", columnDefinition = "TEXT")
    private String organismes;
}
