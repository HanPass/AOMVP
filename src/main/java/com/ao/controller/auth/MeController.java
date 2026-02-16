package com.ao.controller.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    @GetMapping
    public Map<String, Object> me(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();

        return Map.of(
                "subject", jwt.getSubject(),
                "email", jwt.getClaimAsString("email"),
                "preferredUsername", jwt.getClaimAsString("preferred_username"),
                "emailVerified", Boolean.TRUE.equals(jwt.getClaim("email_verified")),
                "roles", roles
        );
    }
}
