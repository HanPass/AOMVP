package com.ao.repository;

import com.ao.entity.AppelOffreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppelOffreRepository extends JpaRepository<AppelOffreEntity, Long> {

    boolean existsByReference(String reference);
}
