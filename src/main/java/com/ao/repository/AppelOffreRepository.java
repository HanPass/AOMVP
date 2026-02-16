package com.ao.repository;

import com.ao.entity.AppelOffreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AppelOffreRepository extends JpaRepository<AppelOffreEntity, Long>, JpaSpecificationExecutor<AppelOffreEntity> {

    boolean existsByReference(String reference);
}
