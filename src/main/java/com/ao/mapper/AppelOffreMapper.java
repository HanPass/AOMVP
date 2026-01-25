package com.ao.mapper;

import com.ao.dto.AppelOffre;
import com.ao.entity.AppelOffreEntity;

public class AppelOffreMapper {

    public static AppelOffreEntity toEntity(AppelOffre ao) {
        return AppelOffreEntity.builder()
                .reference(ao.getReference())
                .objet(ao.getObjet())
                .organisme(ao.getOrganisme())
                .lieuExec(ao.getLieuExec())
                .datePublication(ao.getDatePublication())
                .dateLimite(ao.getDateLimite())
                .urlDetail(ao.getUrlDetail())
                .build();
    }
}
