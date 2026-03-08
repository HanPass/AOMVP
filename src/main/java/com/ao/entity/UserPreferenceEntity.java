package com.ao.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_preference", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_preference_email", columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "preferences_json", nullable = false, columnDefinition = "TEXT")
    private String preferencesJson;
}
