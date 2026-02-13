package com.ao.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequest(
        @NotBlank @Email String email,
        String firstName,
        String lastName,
        Boolean enabled
) {
}
