package com.ao.service;

import com.ao.dto.TenderDto;

import java.util.List;

public interface TenderService {
    int saveIfNew(List<TenderDto> dtos);
}
