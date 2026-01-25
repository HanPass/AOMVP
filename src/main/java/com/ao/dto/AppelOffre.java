package com.ao.dto;

import lombok.*;

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
    private LocalDate datePublication;
    private LocalDateTime dateLimite;
    private String urlDetail;
}
