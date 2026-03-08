package com.ao.controller;

import com.ao.dto.AppelOffre;
import com.ao.service.AppelOffreService;
import com.ao.service.FrontendAuthService;
import com.ao.service.UserPreferenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FrontendCompatControllerTest {

    @Mock
    private FrontendAuthService frontendAuthService;

    @Mock
    private AppelOffreService appelOffreService;

    @Mock
    private UserPreferenceService userPreferenceService;

    @InjectMocks
    private FrontendCompatController controller;

    @Test
    void shouldReturnTokenOnLogin() {
        when(frontendAuthService.login("contact@entreprise.ma")).thenReturn("token-123");

        ResponseEntity<Map<String, String>> response = controller.login(Map.of(
                "email", "contact@entreprise.ma",
                "password", "secret123"
        ));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("token-123", response.getBody().get("token"));
    }

    @Test
    void shouldRegisterWithFallbackCompanyFields() {
        when(frontendAuthService.register("contact@entreprise.ma", "Ma Société")).thenReturn("token-reg");

        ResponseEntity<Map<String, String>> response = controller.register(Map.of(
                "email", "contact@entreprise.ma",
                "company", "Ma Société"
        ));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("token-reg", response.getBody().get("token"));
    }

    @Test
    void shouldFilterAoByQuerySectorAndStatus() {
        AppelOffre open = AppelOffre.builder()
                .reference("AO-1")
                .objet("Maintenance IT")
                .domaine("IT")
                .dateLimite(LocalDateTime.now().plusDays(1))
                .build();
        AppelOffre closed = AppelOffre.builder()
                .reference("AO-2")
                .objet("Travaux")
                .domaine("BTP")
                .dateLimite(LocalDateTime.now().minusDays(1))
                .build();

        when(appelOffreService.getAllAppelOffre()).thenReturn(List.of(open, closed));

        ResponseEntity<List<AppelOffre>> response = controller.getAo("it", "it", "open");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("AO-1", response.getBody().get(0).getReference());
    }

    @Test
    void shouldGetAndSavePreferencesWhenBearerTokenValid() {
        when(frontendAuthService.emailFromToken("abc")).thenReturn("contact@entreprise.ma");
        when(userPreferenceService.getPreferences("contact@entreprise.ma")).thenReturn(Map.of("sector", "IT"));

        ResponseEntity<Map<String, Object>> getResponse = controller.getPreferences("Bearer abc");
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals("IT", getResponse.getBody().get("sector"));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", "open");
        when(userPreferenceService.savePreferences("contact@entreprise.ma", payload)).thenReturn(payload);

        ResponseEntity<Map<String, Object>> putResponse = controller.savePreferences("Bearer abc", payload);
        assertEquals(HttpStatus.OK, putResponse.getStatusCode());
        assertEquals("open", putResponse.getBody().get("status"));
        verify(userPreferenceService).savePreferences("contact@entreprise.ma", payload);
    }
}
