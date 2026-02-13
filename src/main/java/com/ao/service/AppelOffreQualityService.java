package com.ao.service;

import com.ao.dto.AppelOffre;
import com.ao.service.impl.quality.AppelOffreQualityResult;

public interface AppelOffreQualityService {
    AppelOffreQualityResult normalizeAndValidate(AppelOffre ao);
}
