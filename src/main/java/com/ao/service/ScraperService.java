package com.ao.service;

import com.ao.dto.AppelOffre;
import com.ao.dto.TenderDto;

import java.io.IOException;
import java.util.List;

public interface ScraperService {
    List<TenderDto> fetchLatest() throws IOException;
    List<TenderDto> fetchLatestWithPagination(int maxPages) throws IOException;

    List<AppelOffre> fetchAll();
}
