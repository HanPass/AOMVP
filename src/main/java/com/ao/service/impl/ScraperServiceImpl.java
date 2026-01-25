package com.ao.service.impl;

import com.ao.dto.AppelOffre;
import com.ao.dto.TenderDto;
import com.ao.event.NewTenderEvent;
import com.ao.service.ScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScraperServiceImpl implements ScraperService {

    private final ApplicationEventPublisher eventPublisher;
    private static final String BASE_URL =
            "https://www.marchespublics.gov.ma/index.php?page=entreprise.EntrepriseAdvancedSearch&AllCons&statut=publie";

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DateTimeFormatter PUB_DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter DEADLINE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public List<TenderDto> fetchLatest() throws IOException {
            Document doc = Jsoup.connect(
                            "https://www.marchespublics.gov.ma/index.php?page=entreprise.EntrepriseAdvancedSearch&AllCons&statut=publie"
                    )
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120")
                    .timeout(30_000)
                    .get();

            Elements rows = doc.select("table.table-results > tbody > tr");

            log.info("Nombre de lignes détectées: {}", rows.size());

            List<TenderDto> results = new ArrayList<>();

            for (Element row : rows) {

                String sourceId = row.select("input[type=hidden][name$=refCons]")
                        .attr("value");

                String reference = row.select("span.ref").text();

                String title = row.select("div[id$=_panelBlocObjet]")
                        .text()
                        .replace("Objet :", "")
                        .trim();

                String organisme = row.select("div[id$=_panelBlocDenomination]")
                        .text()
                        .replace("Acheteur public :", "")
                        .trim();

                String region = row.select("div[id$=_panelBlocLieuxExec]")
                        .text()
                        .replaceAll("\\s+", " ")
                        .trim();

                String deadline = textOrEmpty(row, "td.col-60 .cloture-line");


                String detailUrl = row
                        .select("td.actions a[href*='EntrepriseDetailConsultation']")
                        .attr("href");

                if (!detailUrl.startsWith("http")) {
                    detailUrl = BASE_URL + detailUrl;
                }

                TenderDto dto = new TenderDto(
                        sourceId,
                        reference,
                        title,
                        organisme,
                        region,
                        deadline,
                        detailUrl
                );

                results.add(dto);

                log.info("AO → {}", dto);
            }

            log.info("AO récupérés: {}", results.size());

        if (results.size() > 0) {
            eventPublisher.publishEvent(
                    new NewTenderEvent(this, results.size())
            );
        }

            return results;
        }

    public List<TenderDto> fetchLatestWithPagination(int maxPages) throws IOException {

        String url = "https://www.marchespublics.gov.ma/index.php?page=entreprise.EntrepriseAdvancedSearch&AllCons&statut=publie";

        List<TenderDto> all = new ArrayList<>();

        Connection connection = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 Chrome/120")
                .timeout(30_000)
                .method(Connection.Method.GET);

        Document doc = connection.get();
        all.addAll(parsePage(doc));

        for (int page = 1   ; page <= maxPages; page++) {

            Map<String, String> data = extractHiddenFields(doc);
            data.put("__EVENTTARGET", "ctl0$CONTENU_PAGE$resultSearch$pager");
            data.put("__EVENTARGUMENT", String.valueOf(page));

            doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 Chrome/120")
                    .timeout(30_000)
                    .method(Connection.Method.POST)
                    .data(data)
                    .post();

            List<TenderDto> pageResults = parsePage(doc);

            log.info("Page {} → {} AO", page, pageResults.size());
            all.addAll(pageResults);

            // sécurité MVP
            if (pageResults.isEmpty()) break;
        }

        return all;
    }

    public List<AppelOffre> fetchAll() {
        List<AppelOffre> results = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(BASE_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120")
                    .timeout(30_000)
                    .get();

            Elements rows = doc.select("table.table-results > tbody > tr");
            log.info("📄 AO détectées: {}", rows.size());

            for (Element row : rows) {
                AppelOffre ao = parseRow(row);
                if (ao != null && ao.getReference() != null && !ao.getReference().isBlank()) {
                    results.add(ao);
                    log.info("AO → {}", ao.getReference());
                }
            }

        } catch (Exception e) {
            log.error("❌ Erreur scraping marchespublics.gov.ma", e);
        }

        log.info("✅ AO récupérées: {}", results.size());
        return results;
    }

    private AppelOffre parseRow(Element row) {

        // ---------- 1) Publié le ----------
        Element tdProc = row.selectFirst("td[headers=cons_ref]");
        String publishedTxt = "";
        if (tdProc != null) {
            Elements divs = tdProc.select("> div");
            if (!divs.isEmpty()) {
                publishedTxt = divs.last().text().trim();
            }
        }
        LocalDate datePublication = parseLocalDateSafe(publishedTxt);

        // ---------- 2) Référence / Objet / Acheteur ----------
        Element tdIntitule = row.selectFirst("td[headers=cons_intitule]");

        String reference = text(tdIntitule, "span.ref");

        String objet = extractObjet(tdIntitule);

        String organisme = cleanupPrefix(
                text(tdIntitule, "div[id$=_panelBlocDenomination]"),
                "Acheteur public :"
        );

        // ---------- 3) Lieu d'execution ----------
        Element tdLieuExec = row.selectFirst("td[headers=cons_lieuExe]");
        String lieuExec = "";

        if (tdLieuExec != null) {
            Element lieuExecDiv = tdLieuExec.selectFirst("div[id$=_panelBlocLieuxExec]");
            if (lieuExecDiv != null) {

                // 1️⃣ Cloner pour ne pas modifier le DOM original
                Element clone = lieuExecDiv.clone();

                // 2️⃣ Supprimer les blocs parasites (bulles, détails)
                clone.select(".bloc-info-bulle").remove();

                // 3️⃣ Récupérer le texte propre
                lieuExec = clone.text()
                        .replace("\u00A0", " ")
                        .replaceAll("\\s+", " ")
                        .trim();
            }
        }


        // ---------- 3) Date limite ----------
        /*Element tdDateEnd = row.selectFirst("td[headers=cons_dateEnd]");
        String deadlineTxt = text(tdDateEnd, "div.cloture-line")
                .replace("\u00A0", " ")
                .replaceAll("\\s+", " ")
                .trim();

        LocalDateTime dateLimite = parseLocalDateTimeSafe(deadlineTxt);*/
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

        // 👉 "04/12/2026 12:00"
        LocalDateTime dateLimite = parseLocalDateTimeSafe(deadlineTxt);

        // ---------- 4) URL détail ----------
        String detailUrl = row
                .select("td.actions a[href*='EntrepriseDetailConsultation']")
                .attr("href");

        if (!detailUrl.startsWith("http")) {
            detailUrl = "https://www.marchespublics.gov.ma/" + detailUrl;
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

    // ==========================================================
    // ====================== HELPERS ===========================
    // ==========================================================

    private String extractObjet(Element tdIntitule) {
        if (tdIntitule == null) return "";

        Element objetDiv = tdIntitule.selectFirst("div[id$=_panelBlocObjet]");
        if (objetDiv == null) return "";

        // ownText() = texte principal sans <strong>, <span>, <div> enfants
        String objet = objetDiv.ownText()
                .replace("\u00A0", " ")
                .trim();

        return objet;
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

    private List<TenderDto> parsePage(Document doc) {
        List<TenderDto> results = new ArrayList<>();

        Elements rows = doc.select("table.table-results > tbody > tr");

        for (Element row : rows) {
            String sourceId = row.select("input[type=hidden][name$=refCons]")
                    .attr("value");

            String reference = row.select("span.ref").text();

            String title = row.select("div[id$=_panelBlocObjet]")
                    .text()
                    .replace("Objet :", "")
                    .trim();

            String organisme = row.select("div[id$=_panelBlocDenomination]")
                    .text()
                    .replace("Acheteur public :", "")
                    .trim();

            String region = row.select("div[id$=_panelBlocLieuxExec]")
                    .text()
                    .replaceAll("\\s+", " ")
                    .trim();

            String deadline = textOrEmpty(row, "td.col-60 .cloture-line");


            String detailUrl = row
                    .select("td.actions a[href*='EntrepriseDetailConsultation']")
                    .attr("href");

            if (!detailUrl.startsWith("http")) {
                detailUrl = BASE_URL + detailUrl;
            }

            TenderDto dto = new TenderDto(
                    sourceId,
                    reference,
                    title,
                    organisme,
                    region,
                    deadline,
                    detailUrl
            );

            results.add(dto);

            log.info("AO → {}", dto);
        }
        return results;
    }


    private Map<String, String> extractHiddenFields(Document doc) {
        Map<String, String> data = new HashMap<>();
        for (Element input : doc.select("input[type=hidden]")) {
            data.put(input.attr("name"), input.attr("value"));
        }
        return data;
    }

    private String textOrEmpty(Element parent, String cssQuery) {
        Element el = parent.selectFirst(cssQuery);
        return el != null ? el.text().trim() : "";
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
