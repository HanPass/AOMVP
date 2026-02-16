package com.ao.service.impl;

import com.ao.dto.AppelOffre;
import com.ao.dto.AppelOffreSearchCriteria;
import com.ao.entity.AppelOffreEntity;
import com.ao.repository.AppelOffreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppelOffreServiceImplTest {

    @Mock
    private AppelOffreRepository repository;

    @InjectMocks
    private AppelOffreServiceImpl service;

    @Test
    void shouldMapSearchResultsToDtoPage() {
        AppelOffreEntity entity = AppelOffreEntity.builder()
                .reference("AO-2026")
                .objet("Acquisition")
                .organisme("Ministere")
                .lieuExec("Rabat")
                .domaine("Informatique")
                .typeMarche("Fournitures")
                .budgetEstime(new BigDecimal("120000.00"))
                .urlDetail("https://example")
                .build();

        when(repository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        AppelOffreSearchCriteria criteria = AppelOffreSearchCriteria.builder()
                .region("rabat")
                .domaine("info")
                .build();

        var result = service.search(criteria, PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        AppelOffre dto = result.getContent().get(0);
        assertEquals("AO-2026", dto.getReference());
        assertEquals("Informatique", dto.getDomaine());
        assertEquals("Fournitures", dto.getTypeMarche());

        ArgumentCaptor<Specification<AppelOffreEntity>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(repository).findAll(specCaptor.capture(), any(PageRequest.class));
    }
}
