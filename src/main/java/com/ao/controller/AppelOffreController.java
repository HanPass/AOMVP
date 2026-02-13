package com.ao.controller;

import com.ao.dto.AppelOffre;
import com.ao.dto.AppelOffreSearchCriteria;
import com.ao.service.AppelOffreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appel-offre")
@RequiredArgsConstructor
@Slf4j
public class AppelOffreController {

    private final AppelOffreService appelOffreService;

    @GetMapping("/all")
    public ResponseEntity<List<AppelOffre>> getAllAppelOffre() {
        return new ResponseEntity<>(appelOffreService.getAllAppelOffre(), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AppelOffre>> search(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String domaine,
            @RequestParam(required = false) String organisme,
            @RequestParam(required = false) String typeMarche,
            @RequestParam(required = false) BigDecimal budgetMin,
            @RequestParam(required = false) BigDecimal budgetMax,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        AppelOffreSearchCriteria criteria = AppelOffreSearchCriteria.builder()
                .region(region)
                .domaine(domaine)
                .organisme(organisme)
                .typeMarche(typeMarche)
                .budgetMin(budgetMin)
                .budgetMax(budgetMax)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .build();

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        return ResponseEntity.ok(appelOffreService.search(criteria, pageable));
    }
}
