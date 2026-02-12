package com.ao.controller;

import com.ao.dto.NotificationPreferenceRequest;
import com.ao.dto.NotificationPreferenceResponse;
import com.ao.service.NotificationPreferenceAdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationPreferenceControllerTest {

    @Mock
    private NotificationPreferenceAdminService adminService;

    @InjectMocks
    private NotificationPreferenceController controller;

    @Test
    void shouldReturnAllPreferences() {
        List<NotificationPreferenceResponse> data = List.of(
                new NotificationPreferenceResponse(1L, "u1@test.com", true, "it", "rabat", "ministere")
        );
        when(adminService.findAll()).thenReturn(data);

        ResponseEntity<List<NotificationPreferenceResponse>> response = controller.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(data, response.getBody());
        verify(adminService).findAll();
    }

    @Test
    void shouldCreatePreference() {
        NotificationPreferenceRequest request = new NotificationPreferenceRequest(
                "u1@test.com", true, "it", "rabat", "ministere"
        );
        NotificationPreferenceResponse saved = new NotificationPreferenceResponse(
                1L, "u1@test.com", true, "it", "rabat", "ministere"
        );
        when(adminService.upsert(request)).thenReturn(saved);

        ResponseEntity<NotificationPreferenceResponse> response = controller.upsert(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(saved, response.getBody());
        verify(adminService).upsert(request);
    }

    @Test
    void shouldReturnNoContentWhenDeleteSucceeded() {
        when(adminService.deleteById(10L)).thenReturn(true);

        ResponseEntity<Void> response = controller.deleteById(10L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(adminService).deleteById(10L);
    }

    @Test
    void shouldReturnNotFoundWhenDeleteMissing() {
        when(adminService.deleteById(10L)).thenReturn(false);

        ResponseEntity<Void> response = controller.deleteById(10L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(adminService).deleteById(10L);
    }
}
