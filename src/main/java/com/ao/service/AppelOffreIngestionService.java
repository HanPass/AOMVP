package com.ao.service;

import com.ao.dto.AppelOffre;

public interface AppelOffreIngestionService {
    boolean ingestIfNew(AppelOffre ao);
}
