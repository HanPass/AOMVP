package com.ao.controller;

import com.ao.dto.PriceEstimateRequest;
import com.ao.dto.PriceEstimateResponse;
import com.ao.service.PriceIntelligenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceIntelligenceControllerTest {

    @Mock
    private PriceIntelligenceService priceIntelligenceService;

    @InjectMocks
    private PriceIntelligenceController controller;

    @Test
    void shouldReturnEstimateResponse() {
        PriceEstimateRequest request = PriceEstimateRequest.builder().domaine("informatique").build();
        PriceEstimateResponse responseBody = PriceEstimateResponse.builder()
                .sampleSize(2)
                .averageBudget(new BigDecimal("120000.00"))
                .featuresUsed(List.of("domaine"))
                .build();

        when(priceIntelligenceService.estimate(request)).thenReturn(responseBody);

        ResponseEntity<PriceEstimateResponse> response = controller.estimate(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseBody, response.getBody());
        verify(priceIntelligenceService).estimate(request);
    }
}
