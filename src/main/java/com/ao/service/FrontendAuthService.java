package com.ao.service;

import com.ao.entity.UserEntity;
import com.ao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FrontendAuthService {

    private static final String TOKEN_PREFIX = "aomvp-front-";

    private final UserRepository userRepository;

    public String login(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(UserEntity.builder().email(email).enabled(true).build()));
        return tokenForEmail(user.getEmail());
    }

    public String register(String email, String companyName) {
        Optional<UserEntity> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            return tokenForEmail(existing.get().getEmail());
        }

        String firstName = companyName;
        UserEntity created = userRepository.save(UserEntity.builder()
                .email(email)
                .firstName(firstName)
                .enabled(true)
                .build());
        return tokenForEmail(created.getEmail());
    }

    public String tokenForEmail(String email) {
        String encoded = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(email.getBytes(StandardCharsets.UTF_8));
        return TOKEN_PREFIX + encoded;
    }

    public String emailFromToken(String token) {
        if (token == null || !token.startsWith(TOKEN_PREFIX)) {
            return null;
        }
        String encoded = token.substring(TOKEN_PREFIX.length());
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(encoded);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
