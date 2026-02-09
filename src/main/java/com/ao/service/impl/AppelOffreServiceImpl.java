package com.ao.service.impl;

import com.ao.dto.AppelOffre;
import com.ao.entity.AppelOffreEntity;
import com.ao.mapper.AppelOffreMapper;
import com.ao.repository.AppelOffreRepository;
import com.ao.service.AppelOffreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppelOffreServiceImpl implements AppelOffreService {

    private final AppelOffreRepository appelOffreRepository;

    @Override
    public List<AppelOffre> getAllAppelOffre() {
        return AppelOffreMapper.toDtos(appelOffreRepository.findAll());
    }
}
