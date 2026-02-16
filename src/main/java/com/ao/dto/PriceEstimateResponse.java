package com.ao.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceEstimateResponse {
    private int sampleSize;
    private BigDecimal minBudget;
    private BigDecimal maxBudget;
    private BigDecimal averageBudget;
    private BigDecimal medianBudget;
    private BigDecimal suggestedBudget;
    private List<String> featuresUsed;
}
