package com.ao.service;

import com.ao.dto.PriceEstimateRequest;
import com.ao.dto.PriceEstimateResponse;

public interface PriceIntelligenceService {
    PriceEstimateResponse estimate(PriceEstimateRequest request);
}
