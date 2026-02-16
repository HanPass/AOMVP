package com.ao.service.impl;

import com.ao.dto.AppelOffre;
import com.ao.service.AppelOffreIngestionService;
import com.ao.service.ScraperDetailService;
import com.ao.service.ScraperJobService;
import com.ao.service.ScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScraperJobServiceImpl implements ScraperJobService {
    private final ScraperService listService;
    private final ScraperDetailService detailService;
    private final AppelOffreIngestionService ingestionService;

    @Value("${app.scraper.max-items-per-run:100}")
    private int maxItemsPerRun = 100;

    public void run() {
        log.info("🚀 Lancement scraper AO");

        int fetched = 0;
        int processed = 0;
        int enriched = 0;
        int inserted = 0;
        int errors = 0;

        List<AppelOffre> list = listService.fetchAll();
        fetched = list.size();

        for (AppelOffre aoLight : list) {
            if (processed >= maxItemsPerRun) {
                log.info("Limite de traitement atteinte: {} AO max par run", maxItemsPerRun);
                break;
            }

            processed++;
            try {
                AppelOffre ao = detailService.enrich(aoLight);
                enriched++;
                if (ingestionService.ingestIfNew(ao)) {
                    inserted++;
                }
            } catch (Exception e) {
                errors++;
                log.error("Erreur AO {}", aoLight.getReference(), e);
            }
        }

        int duplicatesOrIgnored = Math.max(0, enriched - inserted);
        log.info(
                "🏁 Fin scraper AO - fetched={}, processed={}, enriched={}, inserted={}, duplicates_or_ignored={}, errors={}",
                fetched,
                processed,
                enriched,
                inserted,
                duplicatesOrIgnored,
                errors
        );
    }
}
