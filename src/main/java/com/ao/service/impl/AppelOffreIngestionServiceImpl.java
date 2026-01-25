package com.ao.service.impl;

import com.ao.dto.AppelOffre;
import com.ao.repository.AppelOffreRepository;
import com.ao.mapper.AppelOffreMapper;
import com.ao.service.AppelOffreIngestionService;
import com.ao.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppelOffreIngestionServiceImpl implements AppelOffreIngestionService {

    private final AppelOffreRepository repository;
    private final EmailService emailService;

    /**
     * Retourne true si nouvelle AO (mail envoyé), false si déjà connue.
     */
    @Transactional
    public boolean ingestIfNew(AppelOffre ao) {
        if (ao == null || ao.getReference() == null || ao.getReference().isBlank()) {
            log.warn("AO ignorée (référence vide): {}", ao);
            return false;
        }

        if (repository.existsByReference(ao.getReference())) {
            log.debug("AO déjà connue: {}", ao.getReference());
            return false;
        }

        try {
            repository.save(AppelOffreMapper.toEntity(ao));

            log.info("🆕 Nouvelle AO détectée [{}] {}", ao.getReference(), ao.getObjet());
            emailService.sendAlert(ao);

            return true;
        } catch (DataIntegrityViolationException e) {
            // sécurité si 2 threads/process insèrent en même temps
            log.info("AO déjà insérée (race condition) {}", ao.getReference());
            return false;
        }
    }
}

