package com.ao.dto;

public record NotificationPreferenceResponse(
        Long id,
        String email,
        boolean enabled,
        String keywords,
        String regions,
        String organismes
) {
}
