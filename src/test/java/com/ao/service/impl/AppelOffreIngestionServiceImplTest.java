package com.ao.service.impl;

import com.ao.dto.AppelOffre;
import com.ao.entity.AppelOffreEntity;
import com.ao.repository.AppelOffreRepository;
import com.ao.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppelOffreIngestionServiceImplTest {

    @Mock
    private AppelOffreRepository repository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AppelOffreIngestionServiceImpl service;

    @Test
    void shouldReturnFalseWhenAoIsNull() {
        boolean inserted = service.ingestIfNew(null);

        assertFalse(inserted);
        verify(repository, never()).save(any());
        verify(emailService, never()).sendAlert(any());
    }

    @Test
    void shouldReturnFalseWhenReferenceIsBlank() {
        AppelOffre ao = AppelOffre.builder().reference("   ").build();

        boolean inserted = service.ingestIfNew(ao);

        assertFalse(inserted);
        verify(repository, never()).existsByReference(any());
        verify(repository, never()).save(any());
        verify(emailService, never()).sendAlert(any());
    }

    @Test
    void shouldReturnFalseWhenAlreadyExists() {
        AppelOffre ao = AppelOffre.builder().reference("AO-123").build();
        when(repository.existsByReference("AO-123")).thenReturn(true);

        boolean inserted = service.ingestIfNew(ao);

        assertFalse(inserted);
        verify(repository, never()).save(any());
        verify(emailService, never()).sendAlert(any());
    }

    @Test
    void shouldSaveAndAlertWhenNewAo() {
        AppelOffre ao = AppelOffre.builder()
                .reference("AO-999")
                .objet("Nettoyage")
                .build();
        when(repository.existsByReference("AO-999")).thenReturn(false);

        boolean inserted = service.ingestIfNew(ao);

        assertTrue(inserted);

        ArgumentCaptor<AppelOffreEntity> captor = ArgumentCaptor.forClass(AppelOffreEntity.class);
        verify(repository).save(captor.capture());
        assertEquals("AO-999", captor.getValue().getReference());
        assertEquals("Nettoyage", captor.getValue().getObjet());

        verify(emailService).sendAlert(ao);
    }

    @Test
    void shouldReturnFalseOnDataIntegrityViolation() {
        AppelOffre ao = AppelOffre.builder().reference("AO-777").build();
        when(repository.existsByReference("AO-777")).thenReturn(false);
        when(repository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        boolean inserted = service.ingestIfNew(ao);

        assertFalse(inserted);
        verify(emailService, never()).sendAlert(any());
    }
}
