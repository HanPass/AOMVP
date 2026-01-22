package com.ao.service.impl;

import com.ao.dto.TenderDto;
import com.ao.event.NewTenderEvent;
import com.ao.service.ScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScraperServiceImpl implements ScraperService {

    private final ApplicationEventPublisher eventPublisher;
    private static final String BASE_URL = "https://www.marchespublics.gov.ma/";
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


}
