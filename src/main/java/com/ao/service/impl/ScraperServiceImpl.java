package com.ao.service.impl;

import com.ao.dto.AppelOffre;
import com.ao.service.ScraperService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ScraperServiceImpl implements ScraperService {

    private static final String LIST_URL =
            "https://www.marchespublics.gov.ma/index.php?page=entreprise.EntrepriseAdvancedSearch&AllCons&statut=publie";
    private static final String SITE_BASE_URL = "https://www.marchespublics.gov.ma/";

    private static final DateTimeFormatter PUB_DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter DEADLINE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public List<AppelOffre> fetchAll() {
        List<AppelOffre> results = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(LIST_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120")
                    .timeout(30_000)
                    .get();

            Elements rows = doc.select("table.table-results > tbody > tr");
            log.info("📄 AO détectées: {}", rows.size());

            for (Element row : rows) {
                AppelOffre ao = parseRow(row);
                if (ao != null && ao.getReference() != null && !ao.getReference().isBlank()) {
                    results.add(ao);
                }
            }

        } catch (Exception e) {
            log.error("❌ Erreur scraping marchespublics.gov.ma", e);
        }

        log.info("✅ AO récupérées: {}", results.size());
        return results;
    }

    private AppelOffre parseRow(Element row) {

        Element tdProc = row.selectFirst("td[headers=cons_ref]");
        String publishedTxt = "";
        if (tdProc != null) {
            Elements divs = tdProc.select("> div");
            if (!divs.isEmpty()) {
                publishedTxt = divs.last().text().trim();
            }
        }
        LocalDate datePublication = parseLocalDateSafe(publishedTxt);

        Element tdIntitule = row.selectFirst("td[headers=cons_intitule]");

        String reference = text(tdIntitule, "span.ref");
        String objet = extractObjet(tdIntitule);
        String organisme = cleanupPrefix(
                text(tdIntitule, "div[id$=_panelBlocDenomination]"),
                "Acheteur public :"
        );

        Element tdLieuExec = row.selectFirst("td[headers=cons_lieuExe]");
        String lieuExec = "";
        if (tdLieuExec != null) {
            Element lieuExecDiv = tdLieuExec.selectFirst("div[id$=_panelBlocLieuxExec]");
            if (lieuExecDiv != null) {
                Element clone = lieuExecDiv.clone();
                clone.select(".bloc-info-bulle").remove();

                lieuExec = clone.text()
                        .replace("\u00A0", " ")
                        .replaceAll("\\s+", " ")
                        .trim();
            }
        }

        Element tdDateEnd = row.selectFirst("td[headers=cons_dateEnd]");
        String deadlineTxt = "";

        if (tdDateEnd != null) {
            Element clotureDiv = tdDateEnd.selectFirst("div.cloture-line");
            if (clotureDiv != null) {

                StringBuilder sb = new StringBuilder();

                for (Node node : clotureDiv.childNodes()) {
                    if (node instanceof TextNode textNode) {
                        String txt = textNode.text()
                                .replace("\u00A0", " ")
                                .trim();
                        if (!txt.isEmpty()) {
                            if (sb.length() > 0) sb.append(" ");
                            sb.append(txt);
                        }
                    }
                }

                deadlineTxt = sb.toString();
            }
        }

        LocalDateTime dateLimite = parseLocalDateTimeSafe(deadlineTxt);

        String detailUrl = row
                .select("td.actions a[href*='EntrepriseDetailConsultation']")
                .attr("href");

        if (!detailUrl.startsWith("http")) {
            detailUrl = SITE_BASE_URL + detailUrl;
        }

        return AppelOffre.builder()
                .reference(reference)
                .objet(objet)
                .organisme(organisme)
                .lieuExec(lieuExec)
                .datePublication(datePublication)
                .dateLimite(dateLimite)
                .urlDetail(detailUrl)
                .build();
    }

    private String extractObjet(Element tdIntitule) {
        if (tdIntitule == null) return "";

        Element objetDiv = tdIntitule.selectFirst("div[id$=_panelBlocObjet]");
        if (objetDiv == null) return "";

        return objetDiv.ownText()
                .replace("\u00A0", " ")
                .trim();
    }

    private String text(Element root, String css) {
        if (root == null) return "";
        Element el = root.selectFirst(css);
        return el == null ? "" : el.text().trim();
    }

    private String cleanupPrefix(String value, String prefix) {
        if (value == null) return "";
        String v = value.replace("\u00A0", " ").trim();
        if (v.startsWith(prefix)) {
            v = v.substring(prefix.length()).trim();
        }
        return v;
    }

    private LocalDate parseLocalDateSafe(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            return LocalDate.parse(text.trim(), PUB_DATE_FMT);
        } catch (Exception e) {
            log.warn("Date publication non parsable: '{}'", text);
            return null;
        }
    }

    private LocalDateTime parseLocalDateTimeSafe(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            return LocalDateTime.parse(text.trim(), DEADLINE_FMT);
        } catch (Exception e) {
            log.warn("Date limite non parsable: '{}'", text);
            return null;
        }
    }
}
