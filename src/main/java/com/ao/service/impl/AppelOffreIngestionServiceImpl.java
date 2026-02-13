package com.ao.service.impl;

import com.ao.dto.AppelOffre;
import com.ao.mapper.AppelOffreMapper;
import com.ao.repository.AppelOffreRepository;
import com.ao.service.AppelOffreIngestionService;
import com.ao.service.AppelOffreQualityService;
import com.ao.service.EmailService;
import com.ao.service.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppelOffreIngestionServiceImpl implements AppelOffreIngestionService {

    private final AppelOffreRepository repository;
    private final EmailService emailService;
    private final NotificationPreferenceService notificationPreferenceService;
    private final AppelOffreQualityService qualityService;

    /**
     * Retourne true si nouvelle AO (mail préparé/éventuellement envoyé), false si déjà connue/invalide.
     */
    @Transactional
    public boolean ingestIfNew(AppelOffre ao) {
        var quality = qualityService.normalizeAndValidate(ao);
        if (!quality.isValid()) {
            log.warn("AO ignorée (qualité insuffisante): {}", quality.issues());
            return false;
        }

        AppelOffre normalized = quality.normalized();

        if (repository.existsByReference(normalized.getReference())) {
            log.debug("AO déjà connue: {}", normalized.getReference());
            return false;
        }

        try {
            repository.save(AppelOffreMapper.toEntity(normalized));
            log.info("🆕 Nouvelle AO détectée [{}] {}", normalized.getReference(), normalized.getObjet());

            List<String> recipients = notificationPreferenceService.findMatchingRecipientEmails(normalized);
            if (recipients.isEmpty()) {
                log.info("Aucun destinataire correspondant aux préférences pour {}", normalized.getReference());
            } else {
                recipients.forEach(recipient -> emailService.sendAlert(normalized, recipient));
                log.info("Notification traitée pour {} destinataire(s) sur {}", recipients.size(), normalized.getReference());
            }

            return true;
        } catch (DataIntegrityViolationException e) {
            // sécurité si 2 threads/process insèrent en même temps
            log.info("AO déjà insérée (race condition) {}", normalized.getReference());
            return false;
        }
    }
}
