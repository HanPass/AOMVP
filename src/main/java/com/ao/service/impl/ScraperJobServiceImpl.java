package com.ao.service.impl;

import com.ao.service.AppelOffreIngestionService;
import com.ao.service.ScraperDetailService;
import com.ao.service.ScraperJobService;
import com.ao.service.ScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScraperJobServiceImpl implements ScraperJobService {
    private final ScraperService listService;
    private final ScraperDetailService detailService;
    private final AppelOffreIngestionService ingestionService;

    public void run() {
        log.info("🚀 Lancement scraper AO");

        listService.fetchAll().forEach(aoLight -> {
            try {
                var ao = detailService.enrich(aoLight);
                ingestionService.ingestIfNew(ao);
            } catch (Exception e) {
                log.error("Erreur AO {}", aoLight.getReference(), e);
            }
        });

        log.info("🏁 Fin scraper AO");
    }
}
