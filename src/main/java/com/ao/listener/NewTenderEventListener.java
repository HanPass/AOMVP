package com.ao.listener;

import com.ao.event.NewTenderEvent;
import com.ao.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewTenderEventListener {

    private final EmailService emailService;

    @EventListener
    public void handleNewTenderEvent(NewTenderEvent event) {

        int count = event.getNewCount();

        log.info("Event reçu – {} nouveaux AO", count);

        //TODO
        emailService.sendAlert(
                "📢 Nouveaux appels d’offres détectés",
                "🚀 " + count + " nouveaux appels d’offres ont été ajoutés."
        );
    }
}
