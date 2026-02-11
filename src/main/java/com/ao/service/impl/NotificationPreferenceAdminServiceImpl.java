package com.ao.service.impl;

import com.ao.dto.NotificationPreferenceRequest;
import com.ao.dto.NotificationPreferenceResponse;
import com.ao.entity.NotificationPreferenceEntity;
import com.ao.repository.NotificationPreferenceRepository;
import com.ao.service.NotificationPreferenceAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceAdminServiceImpl implements NotificationPreferenceAdminService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;

    @Override
    @Transactional
    public NotificationPreferenceResponse upsert(NotificationPreferenceRequest request) {
        NotificationPreferenceEntity entity = notificationPreferenceRepository.findByEmail(request.email())
                .orElseGet(NotificationPreferenceEntity::new);

        entity.setEmail(request.email().trim());
        entity.setEnabled(request.enabled() == null || request.enabled());
        entity.setKeywords(request.keywords());
        entity.setRegions(request.regions());
        entity.setOrganismes(request.organismes());

        NotificationPreferenceEntity saved = notificationPreferenceRepository.save(entity);

        return toResponse(saved);
    }

    @Override
    public List<NotificationPreferenceResponse> findAll() {
        return notificationPreferenceRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private NotificationPreferenceResponse toResponse(NotificationPreferenceEntity entity) {
        return new NotificationPreferenceResponse(
                entity.getId(),
                entity.getEmail(),
                entity.isEnabled(),
                entity.getKeywords(),
                entity.getRegions(),
                entity.getOrganismes()
        );
    }
}
