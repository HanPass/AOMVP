package com.ao.service;

import com.ao.dto.AppelOffre;
import com.ao.dto.AppelOffreSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AppelOffreService {

    List<AppelOffre> getAllAppelOffre();

    Page<AppelOffre> search(AppelOffreSearchCriteria criteria, Pageable pageable);
}
