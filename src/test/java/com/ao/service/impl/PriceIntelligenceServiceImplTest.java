package com.ao.service.impl;

import com.ao.dto.PriceEstimateRequest;
import com.ao.dto.PriceEstimateResponse;
import com.ao.entity.AppelOffreEntity;
import com.ao.repository.AppelOffreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceIntelligenceServiceImplTest {

    @Mock
    private AppelOffreRepository repository;

    @InjectMocks
    private PriceIntelligenceServiceImpl service;

    @Test
    void shouldReturnEstimateStatsFromHistoricalBudgets() {
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(
                AppelOffreEntity.builder().budgetEstime(new BigDecimal("100000")).build(),
                AppelOffreEntity.builder().budgetEstime(new BigDecimal("140000")).build(),
                AppelOffreEntity.builder().budgetEstime(new BigDecimal("160000")).build()
        ));

        PriceEstimateRequest request = PriceEstimateRequest.builder()
                .domaine("informatique")
                .budgetHint(new BigDecimal("150000"))
                .build();

        PriceEstimateResponse response = service.estimate(request);

        assertEquals(3, response.getSampleSize());
        assertEquals(new BigDecimal("100000"), response.getMinBudget());
        assertEquals(new BigDecimal("160000"), response.getMaxBudget());
        assertEquals(new BigDecimal("133333.33"), response.getAverageBudget());
        assertEquals(new BigDecimal("140000"), response.getMedianBudget());
        assertEquals(new BigDecimal("145000.00"), response.getSuggestedBudget());
        assertTrue(response.getFeaturesUsed().contains("domaine"));
        assertTrue(response.getFeaturesUsed().contains("budgetHint"));
    }

    @Test
    void shouldReturnEmptyEstimateWhenNoHistoricalBudget() {
        when(repository.findAll(any(Specification.class))).thenReturn(List.of());

        PriceEstimateResponse response = service.estimate(PriceEstimateRequest.builder().build());

        assertEquals(0, response.getSampleSize());
    }
}
