package com.ao.controller;

import com.ao.controller.auth.MeController;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MeControllerTest {

    private final MeController controller = new MeController();

    @Test
    void shouldReturnBasicIdentityPayload() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(600),
                Map.of("alg", "none"),
                Map.of(
                        "sub", "123",
                        "email", "admin@aomvp.local",
                        "preferred_username", "admin",
                        "email_verified", true
                )
        );

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                jwt,
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"))
        );

        Map<String, Object> me = controller.me(auth);

        assertEquals("123", me.get("subject"));
        assertEquals("admin@aomvp.local", me.get("email"));
        assertEquals("admin", me.get("preferredUsername"));
        assertEquals(true, me.get("emailVerified"));
        assertTrue(((List<?>) me.get("roles")).contains("ROLE_ADMIN"));
    }
}
