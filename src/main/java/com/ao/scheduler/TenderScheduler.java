package com.ao.scheduler;

import com.ao.dto.TenderDto;
import com.ao.entity.Tender;
import com.ao.service.ScraperService;
import com.ao.service.TenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenderScheduler {

    private final ScraperService scraper;
    private final TenderService tenderService;

    //@Scheduled(cron = "0 0 */6 * * *") // toutes les 6h
    @Scheduled(fixedDelay = 30_000)
    public void collectTenders() {
        log.info("SCRAPPING STARTED");
        try {
            List<TenderDto> dtos = scraper.fetchLatest();
            tenderService.saveIfNew(dtos);
        } catch (Exception e) {
            log.error("Erreur collecte AO", e);
        }
        log.info("SCRAPPING ENDED");
    }
}

