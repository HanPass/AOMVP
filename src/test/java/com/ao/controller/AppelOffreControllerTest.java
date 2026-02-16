package com.ao.controller;

import com.ao.dto.AppelOffre;
import com.ao.dto.AppelOffreSearchCriteria;
import com.ao.service.AppelOffreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void shouldSearchAoWithPaginationAndFilters() {
        Page<AppelOffre> page = new PageImpl<>(List.of(AppelOffre.builder().reference("AO-99").build()));
        when(appelOffreService.search(any(AppelOffreSearchCriteria.class), any())).thenReturn(page);

        ResponseEntity<Page<AppelOffre>> response = controller.search(
                "rabat", "informatique", "ministere", "travaux",
                null, null, null, null,
                0, 10
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(page, response.getBody());

        ArgumentCaptor<AppelOffreSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(AppelOffreSearchCriteria.class);
        ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(appelOffreService).search(criteriaCaptor.capture(), pageCaptor.capture());

        assertEquals("rabat", criteriaCaptor.getValue().getRegion());
        assertEquals("informatique", criteriaCaptor.getValue().getDomaine());
        assertEquals(0, pageCaptor.getValue().getPageNumber());
        assertEquals(10, pageCaptor.getValue().getPageSize());
    }
}
