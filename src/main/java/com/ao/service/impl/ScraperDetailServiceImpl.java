package com.ao.service.impl;

import com.ao.dto.AppelOffre;
import com.ao.service.ScraperDetailService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class ScraperDetailServiceImpl implements ScraperDetailService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public AppelOffre enrich(AppelOffre ao) {
        try {
            Document doc = Jsoup.connect(ao.getUrlDetail())
                    .userAgent("Mozilla/5.0")
                    .timeout(30_000)
                    .get();

            // ⚠️ Sur la page détail, l'organisme n'est pas toujours présent
            String organisme = doc.select("div[id$=_panelBlocDenomination]")
                    .text()
                    .replace("Acheteur public :", "")
                    .trim();

            if (!organisme.isBlank()) {
                ao.setOrganisme(organisme);
            }

            // ✅ BON SELECTEUR
            String dateText = doc.select("div.cloture-line")
                    .text()
                    .replace("\u00A0", " ")
                    .trim();

            if (ao.getDateLimite() == null) {
                LocalDateTime dateLimite = parseDateLimite(dateText);
                ao.setDateLimite(dateLimite);
            }


        } catch (Exception e) {
            log.warn("Erreur détail AO {}", ao.getReference(), e);
        }
        return ao;
    }

    private LocalDateTime parseDateLimite(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(text, DATE_TIME_FORMATTER);
        } catch (Exception e) {
            log.warn("Date limite non parsable: '{}'", text);
            return null;
        }
    }
}
