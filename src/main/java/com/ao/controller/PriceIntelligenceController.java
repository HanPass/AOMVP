package com.ao.controller;

import com.ao.dto.PriceEstimateRequest;
import com.ao.dto.PriceEstimateResponse;
import com.ao.service.PriceIntelligenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/price-intelligence")
@RequiredArgsConstructor
public class PriceIntelligenceController {

    private final PriceIntelligenceService priceIntelligenceService;

    @PostMapping("/estimate")
    public ResponseEntity<PriceEstimateResponse> estimate(@RequestBody PriceEstimateRequest request) {
        return ResponseEntity.ok(priceIntelligenceService.estimate(request));
    }
}
