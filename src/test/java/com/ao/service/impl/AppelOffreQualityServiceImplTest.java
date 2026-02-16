package com.ao.service.impl;

import com.ao.dto.AppelOffre;
import com.ao.service.impl.quality.AppelOffreQualityResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AppelOffreQualityServiceImplTest {

    private final AppelOffreQualityServiceImpl service = new AppelOffreQualityServiceImpl();

    @Test
    void shouldNormalizeAndValidateValidAo() {
        AppelOffre ao = AppelOffre.builder()
                .reference(" ao-12 ")
                .objet("  Nettoyage   des  locaux ")
                .organisme(" Commune   X ")
                .lieuExec(" Rabat ")
                .urlDetail("  https://example.com/detail ")
                .datePublication(LocalDate.of(2026, 1, 10))
                .dateLimite(LocalDateTime.of(2026, 2, 10, 10, 0))
                .build();

        AppelOffreQualityResult result = service.normalizeAndValidate(ao);

        assertTrue(result.isValid());
        assertEquals("AO-12", result.normalized().getReference());
        assertEquals("Nettoyage des locaux", result.normalized().getObjet());
        assertEquals("https://example.com/detail", result.normalized().getUrlDetail());
    }

    @Test
    void shouldReturnIssuesForInvalidAo() {
        AppelOffre ao = AppelOffre.builder()
                .reference(" ")
                .objet(" ")
                .urlDetail("ftp://bad-url")
                .datePublication(LocalDate.of(2026, 2, 10))
                .dateLimite(LocalDateTime.of(2026, 1, 10, 10, 0))
                .build();

        AppelOffreQualityResult result = service.normalizeAndValidate(ao);

        assertFalse(result.isValid());
        assertTrue(result.issues().contains("REFERENCE_EMPTY"));
        assertTrue(result.issues().contains("OBJET_EMPTY"));
        assertTrue(result.issues().contains("URL_DETAIL_INVALID"));
        assertTrue(result.issues().contains("DATE_INCOHERENT"));
    }
}
