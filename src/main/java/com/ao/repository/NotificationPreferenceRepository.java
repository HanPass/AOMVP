package com.ao.repository;

import com.ao.entity.NotificationPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreferenceEntity, Long> {
    List<NotificationPreferenceEntity> findByEnabledTrue();

    Optional<NotificationPreferenceEntity> findByEmail(String email);
}
