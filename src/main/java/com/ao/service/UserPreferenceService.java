package com.ao.service;

import com.ao.entity.UserPreferenceEntity;
import com.ao.repository.UserPreferenceRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final UserPreferenceRepository userPreferenceRepository;
    private final ObjectMapper objectMapper;

    public Map<String, Object> getPreferences(String email) {
        return userPreferenceRepository.findByEmail(email)
                .map(UserPreferenceEntity::getPreferencesJson)
                .map(this::readJson)
                .orElseGet(LinkedHashMap::new);
    }

    public Map<String, Object> savePreferences(String email, Map<String, Object> preferences) {
        String json = writeJson(preferences);
        UserPreferenceEntity entity = userPreferenceRepository.findByEmail(email)
                .orElseGet(() -> UserPreferenceEntity.builder().email(email).build());
        entity.setPreferencesJson(json);
        userPreferenceRepository.save(entity);
        return preferences;
    }

    private Map<String, Object> readJson(String json) {
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception ex) {
            return new LinkedHashMap<>();
        }
    }

    private String writeJson(Map<String, Object> preferences) {
        try {
            return objectMapper.writeValueAsString(preferences);
        } catch (Exception ex) {
            return "{}";
        }
    }
}
