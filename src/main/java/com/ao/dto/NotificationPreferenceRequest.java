package com.ao.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record NotificationPreferenceRequest(
        @NotBlank @Email String email,
        Boolean enabled,
        String keywords,
        String regions,
        String organismes
) {
}
