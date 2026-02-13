package com.ao.service.impl;

import com.ao.dto.NotificationPreferenceRequest;
import com.ao.dto.NotificationPreferenceResponse;
import com.ao.entity.NotificationPreferenceEntity;
import com.ao.repository.NotificationPreferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationPreferenceAdminServiceImplTest {

    @Mock
    private NotificationPreferenceRepository repository;

    @InjectMocks
    private NotificationPreferenceAdminServiceImpl service;

    @Test
    void shouldCreatePreferenceWhenEmailNotExisting() {
        NotificationPreferenceRequest request = new NotificationPreferenceRequest(
                " user@test.com ", true, "informatique", "rabat", "ministère"
        );

        NotificationPreferenceEntity saved = NotificationPreferenceEntity.builder()
                .id(1L)
                .email("user@test.com")
                .enabled(true)
                .keywords("informatique")
                .regions("rabat")
                .organismes("ministère")
                .build();

        when(repository.findByEmail("user@test.com")).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(saved);

        NotificationPreferenceResponse response = service.upsert(request);

        assertEquals(1L, response.id());
        assertEquals("user@test.com", response.email());
        assertTrue(response.enabled());
    }

    @Test
    void shouldReturnAllPreferences() {
        when(repository.findAll()).thenReturn(List.of(
                NotificationPreferenceEntity.builder().id(1L).email("a@test.com").enabled(true).build(),
                NotificationPreferenceEntity.builder().id(2L).email("b@test.com").enabled(false).build()
        ));

        List<NotificationPreferenceResponse> responses = service.findAll();

        assertEquals(2, responses.size());
        assertEquals("a@test.com", responses.get(0).email());
        assertEquals("b@test.com", responses.get(1).email());
    }

    @Test
    void shouldDeleteWhenIdExists() {
        when(repository.existsById(5L)).thenReturn(true);

        boolean deleted = service.deleteById(5L);

        assertTrue(deleted);
        verify(repository).deleteById(5L);
    }

    @Test
    void shouldNotDeleteWhenIdMissing() {
        when(repository.existsById(99L)).thenReturn(false);

        boolean deleted = service.deleteById(99L);

        assertFalse(deleted);
    }
}
