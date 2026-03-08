package com.ao.controller;

import com.ao.dto.AppelOffre;
import com.ao.service.AppelOffreService;
import com.ao.service.FrontendAuthService;
import com.ao.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FrontendCompatController {

    private final FrontendAuthService frontendAuthService;
    private final AppelOffreService appelOffreService;
    private final UserPreferenceService userPreferenceService;

    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, Object> payload) {
        String email = text(payload.get("email"));
        if (email == null) {
            return ResponseEntity.badRequest().build();
        }

        String token = frontendAuthService.login(email);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping({"/auth/register", "/auth/signup", "/users/register", "/users/signup"})
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, Object> payload) {
        String email = text(payload.get("email"));
        if (email == null) {
            return ResponseEntity.badRequest().build();
        }

        String companyName = firstNonBlank(
                text(payload.get("companyName")),
                text(payload.get("company")),
                text(payload.get("name")),
                text(payload.get("fullName"))
        );

        String token = frontendAuthService.register(email, companyName);
        return new ResponseEntity<>(Map.of("token", token), HttpStatus.CREATED);
    }

    @GetMapping("/ao")
    public ResponseEntity<List<AppelOffre>> getAo(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String status
    ) {
        List<AppelOffre> all = appelOffreService.getAllAppelOffre();
        String queryValue = normalize(query);
        String sectorValue = normalize(sector);
        String statusValue = normalize(status);

        List<AppelOffre> filtered = all.stream()
                .filter(ao -> matchesQuery(ao, queryValue))
                .filter(ao -> matchesSector(ao, sectorValue))
                .filter(ao -> matchesStatus(ao, statusValue))
                .toList();

        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/users/me/preferences")
    public ResponseEntity<Map<String, Object>> getPreferences(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        String email = emailFromAuthorization(authorization);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(userPreferenceService.getPreferences(email));
    }

    @PutMapping("/users/me/preferences")
    public ResponseEntity<Map<String, Object>> savePreferences(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> preferences
    ) {
        String email = emailFromAuthorization(authorization);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> request = preferences == null ? new LinkedHashMap<>() : preferences;
        return ResponseEntity.ok(userPreferenceService.savePreferences(email, request));
    }

    private String emailFromAuthorization(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return frontendAuthService.emailFromToken(authorization.substring(7));
    }

    private String text(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }

    private boolean matchesQuery(AppelOffre ao, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        return contains(ao.getReference(), query)
                || contains(ao.getObjet(), query)
                || contains(ao.getOrganisme(), query)
                || contains(ao.getDomaine(), query);
    }

    private boolean matchesSector(AppelOffre ao, String sector) {
        return sector == null || sector.isBlank() || contains(ao.getDomaine(), sector);
    }

    private boolean matchesStatus(AppelOffre ao, String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        if ("open".equals(status)) {
            return ao.getDateLimite() == null || ao.getDateLimite().isAfter(java.time.LocalDateTime.now());
        }
        if ("closed".equals(status)) {
            return ao.getDateLimite() != null && !ao.getDateLimite().isAfter(java.time.LocalDateTime.now());
        }
        return true;
    }

    private boolean contains(String source, String expected) {
        return source != null && source.toLowerCase().contains(expected);
    }
}
