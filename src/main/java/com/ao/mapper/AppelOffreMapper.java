package com.ao.mapper;

import com.ao.dto.AppelOffre;
import com.ao.entity.AppelOffreEntity;

import java.util.List;

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

    public static AppelOffre toDto(AppelOffreEntity ao) {
        return AppelOffre.builder()
                .reference(ao.getReference())
                .organisme(ao.getOrganisme())
                .objet(ao.getObjet())
                .lieuExec(ao.getLieuExec())
                .datePublication(ao.getDatePublication())
                .dateLimite(ao.getDateLimite())
                .urlDetail(ao.getUrlDetail())
                .build();
    }

    public static List<AppelOffre> toDtos(List<AppelOffreEntity> aos) {
        return aos.stream().map(AppelOffreMapper::toDto).toList();
    }
}
