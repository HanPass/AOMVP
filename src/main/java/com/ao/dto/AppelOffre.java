package com.ao.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AppelOffre {
    private String reference;
    private String objet;
    private String organisme;
    private String lieuExec;
    private String domaine;
    private String typeMarche;
    private BigDecimal budgetEstime;
    private LocalDate datePublication;
    private LocalDateTime dateLimite;
    private String urlDetail;
}
