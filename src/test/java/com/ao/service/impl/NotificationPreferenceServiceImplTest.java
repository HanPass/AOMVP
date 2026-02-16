package com.ao.service.impl;

import com.ao.dto.AppelOffre;
import com.ao.entity.NotificationPreferenceEntity;
import com.ao.repository.NotificationPreferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationPreferenceServiceImplTest {

    @Mock
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @InjectMocks
    private NotificationPreferenceServiceImpl service;

    @Test
    void shouldReturnAllEnabledRecipientsWhenNoFiltersProvided() {
        AppelOffre ao = AppelOffre.builder()
                .objet("Travaux d'entretien")
                .lieuExec("Rabat")
                .organisme("Commune X")
                .build();

        NotificationPreferenceEntity p1 = NotificationPreferenceEntity.builder()
                .email("a@test.com")
                .enabled(true)
                .build();
        NotificationPreferenceEntity p2 = NotificationPreferenceEntity.builder()
                .email("b@test.com")
                .enabled(true)
                .build();

        when(notificationPreferenceRepository.findByEnabledTrue()).thenReturn(List.of(p1, p2));

        List<String> recipients = service.findMatchingRecipientEmails(ao);

        assertEquals(List.of("a@test.com", "b@test.com"), recipients);
    }

    @Test
    void shouldFilterByKeywordRegionAndOrganisme() {
        AppelOffre ao = AppelOffre.builder()
                .objet("Acquisition matériel informatique")
                .lieuExec("Rabat-Salé")
                .organisme("Ministère de l'équipement")
                .build();

        NotificationPreferenceEntity ok = NotificationPreferenceEntity.builder()
                .email("ok@test.com")
                .keywords("informatique")
                .regions("rabat")
                .organismes("equipement")
                .enabled(true)
                .build();

        NotificationPreferenceEntity wrongRegion = NotificationPreferenceEntity.builder()
                .email("ko@test.com")
                .keywords("informatique")
                .regions("casablanca")
                .organismes("équipement")
                .enabled(true)
                .build();

        when(notificationPreferenceRepository.findByEnabledTrue()).thenReturn(List.of(ok, wrongRegion));

        List<String> recipients = service.findMatchingRecipientEmails(ao);

        assertEquals(List.of("ok@test.com"), recipients);
    }

    @Test
    void shouldSupportCommaSemicolonAndNewlineTokenSeparators() {
        AppelOffre ao = AppelOffre.builder()
                .objet("Services de nettoyage industriel")
                .lieuExec("Casablanca")
                .organisme("Commune Urbaine")
                .build();

        NotificationPreferenceEntity pref = NotificationPreferenceEntity.builder()
                .email("multi@test.com")
                .keywords("informatique;nettoyage\ntransport")
                .regions("rabat,casablanca")
                .organismes("ministere;commune")
                .enabled(true)
                .build();

        when(notificationPreferenceRepository.findByEnabledTrue()).thenReturn(List.of(pref));

        List<String> recipients = service.findMatchingRecipientEmails(ao);

        assertEquals(List.of("multi@test.com"), recipients);
    }
}
