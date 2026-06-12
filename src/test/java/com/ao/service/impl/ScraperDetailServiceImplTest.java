package com.ao.service.impl;

import com.ao.dto.AppelOffre;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScraperDetailServiceImplTest {

    private final ScraperDetailServiceImpl service = new ScraperDetailServiceImpl();

    @Test
    void shouldExtractBusinessFieldsFromTableLikeDetailPage() {
        Document document = Jsoup.parse("""
                <html><body>
                  <div id="x_panelBlocDenomination">Acheteur public : Commune de Rabat</div>
                  <div class="cloture-line">22/09/2026 10:00</div>
                  <table>
                    <tr><td>Domaine d'activité</td><td>Travaux</td></tr>
                    <tr><td>Type de marché</td><td>Marché de travaux</td></tr>
                    <tr><td>Estimation (en Dhs TTC)</td><td>1 250 000,50 DH</td></tr>
                  </table>
                </body></html>
                """);
        AppelOffre ao = AppelOffre.builder().reference("AO-1").build();

        service.enrichFromDocument(ao, document);

        assertEquals("Commune de Rabat", ao.getOrganisme());
        assertEquals(LocalDateTime.of(2026, 9, 22, 10, 0), ao.getDateLimite());
        assertEquals("Travaux", ao.getDomaine());
        assertEquals("Marché de travaux", ao.getTypeMarche());
        assertEquals(new BigDecimal("1250000.50"), ao.getBudgetEstime());
    }

    @Test
    void shouldExtractBusinessFieldsFromInlineLabels() {
        Document document = Jsoup.parse("""
                <html><body>
                  <div>Domaine : Informatique</div>
                  <div>Nature de marché : Fournitures</div>
                  <div>Montant estimé : 980.000,00 DHS TTC</div>
                </body></html>
                """);
        AppelOffre ao = AppelOffre.builder().reference("AO-2").build();

        service.enrichFromDocument(ao, document);

        assertEquals("Informatique", ao.getDomaine());
        assertEquals("Fournitures", ao.getTypeMarche());
        assertEquals(new BigDecimal("980000.00"), ao.getBudgetEstime());
    }
}
