package com.ao.scheduler;

import com.ao.service.ScraperJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenderScheduler {

    private final ScraperJobService scraperJobService;

    //@Scheduled(cron = "0 0 */6 * * *") // toutes les 6h
    @Scheduled(fixedDelayString = "${app.scraper.fixed-delay-ms:30000}")
    public void collectTenders() {
        log.info("SCRAPPING STARTED");
        scraperJobService.run();
        log.info("SCRAPPING ENDED");
    }
}
