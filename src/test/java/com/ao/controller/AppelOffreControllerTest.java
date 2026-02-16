package com.ao.controller;

import com.ao.dto.AppelOffre;
import com.ao.service.AppelOffreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppelOffreControllerTest {

    @Mock
    private AppelOffreService appelOffreService;

    @InjectMocks
    private AppelOffreController controller;

    @Test
    void shouldReturnAllAoWithStatusOk() {
        List<AppelOffre> expected = List.of(
                AppelOffre.builder().reference("AO-1").build(),
                AppelOffre.builder().reference("AO-2").build()
        );
        when(appelOffreService.getAllAppelOffre()).thenReturn(expected);

        ResponseEntity<List<AppelOffre>> response = controller.getAllAppelOffre();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
        verify(appelOffreService).getAllAppelOffre();
    }
}
