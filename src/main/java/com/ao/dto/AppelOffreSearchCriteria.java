package com.ao.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppelOffreSearchCriteria {
    private String region;
    private String domaine;
    private String organisme;
    private String typeMarche;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
