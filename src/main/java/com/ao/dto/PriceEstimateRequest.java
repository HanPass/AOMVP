package com.ao.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceEstimateRequest {
    private String domaine;
    private String typeMarche;
    private String region;
    private String organisme;
    private LocalDate publicationFrom;
    private LocalDate publicationTo;
    private BigDecimal budgetHint;
}
