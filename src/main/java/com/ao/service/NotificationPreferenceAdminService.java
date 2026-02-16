package com.ao.service;

import com.ao.dto.NotificationPreferenceRequest;
import com.ao.dto.NotificationPreferenceResponse;

import java.util.List;

public interface NotificationPreferenceAdminService {
    NotificationPreferenceResponse upsert(NotificationPreferenceRequest request);

    List<NotificationPreferenceResponse> findAll();

    boolean deleteById(Long id);
}
