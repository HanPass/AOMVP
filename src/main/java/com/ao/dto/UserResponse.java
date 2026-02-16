package com.ao.dto;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        boolean enabled
) {
}
