package com.ao.service.impl;

import com.ao.dto.AppelOffre;
import com.ao.entity.NotificationPreferenceEntity;
import com.ao.repository.NotificationPreferenceRepository;
import com.ao.service.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;

    @Override
    public List<String> findMatchingRecipientEmails(AppelOffre ao) {
        if (ao == null) {
            return List.of();
        }

        return notificationPreferenceRepository.findByEnabledTrue().stream()
                .filter(pref -> matches(pref, ao))
                .map(NotificationPreferenceEntity::getEmail)
                .filter(email -> email != null && !email.isBlank())
                .distinct()
                .toList();
    }

    private boolean matches(NotificationPreferenceEntity pref, AppelOffre ao) {
        String objet = normalize(ao.getObjet());
        String region = normalize(ao.getLieuExec());
        String organisme = normalize(ao.getOrganisme());

        boolean keywordOk = matchAnyToken(pref.getKeywords(), objet);
        boolean regionOk = matchAnyToken(pref.getRegions(), region);
        boolean organismeOk = matchAnyToken(pref.getOrganismes(), organisme);

        return keywordOk && regionOk && organismeOk;
    }

    private boolean matchAnyToken(String csvTokens, String sourceText) {
        List<String> tokens = tokenize(csvTokens);
        if (tokens.isEmpty()) {
            return true;
        }
        return tokens.stream().anyMatch(sourceText::contains);
    }

    private List<String> tokenize(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }

        return Arrays.stream(csv.split(","))
                .map(this::normalize)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT).trim();
    }
}
