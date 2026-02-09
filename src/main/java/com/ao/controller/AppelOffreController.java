package com.ao.controller;

import com.ao.dto.AppelOffre;
import com.ao.service.AppelOffreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
