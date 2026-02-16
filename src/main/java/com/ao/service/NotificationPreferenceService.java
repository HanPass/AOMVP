package com.ao.service;

import com.ao.dto.AppelOffre;

import java.util.List;

public interface NotificationPreferenceService {
    List<String> findMatchingRecipientEmails(AppelOffre ao);
}
