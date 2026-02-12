package com.ao.service.impl;

import com.ao.dto.AppelOffre;
import com.ao.service.AppelOffreQualityService;
import com.ao.service.impl.quality.AppelOffreQualityResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AppelOffreQualityServiceImpl implements AppelOffreQualityService {

    @Override
    public AppelOffreQualityResult normalizeAndValidate(AppelOffre ao) {
        if (ao == null) {
            return new AppelOffreQualityResult(null, List.of("AO_NULL"));
        }

        AppelOffre normalized = AppelOffre.builder()
                .reference(normalizeRef(ao.getReference()))
                .objet(normalizeText(ao.getObjet()))
                .organisme(normalizeText(ao.getOrganisme()))
                .lieuExec(normalizeText(ao.getLieuExec()))
                .datePublication(ao.getDatePublication())
                .dateLimite(ao.getDateLimite())
                .urlDetail(normalizeUrl(ao.getUrlDetail()))
                .build();

        List<String> issues = new ArrayList<>();

        if (isBlank(normalized.getReference())) {
            issues.add("REFERENCE_EMPTY");
        }
        if (isBlank(normalized.getObjet())) {
            issues.add("OBJET_EMPTY");
        }
        if (isBlank(normalized.getUrlDetail())) {
            issues.add("URL_DETAIL_EMPTY");
        } else if (!normalized.getUrlDetail().startsWith("http")) {
            issues.add("URL_DETAIL_INVALID");
        }

        if (normalized.getDatePublication() != null && normalized.getDateLimite() != null
                && normalized.getDateLimite().toLocalDate().isBefore(normalized.getDatePublication())) {
            issues.add("DATE_INCOHERENT");
        }

        return new AppelOffreQualityResult(normalized, issues);
    }

    private String normalizeRef(String value) {
        if (isBlank(value)) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toUpperCase();
    }

    private String normalizeText(String value) {
        if (isBlank(value)) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeUrl(String value) {
        if (isBlank(value)) {
            return "";
        }
        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
