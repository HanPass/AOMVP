package com.ao.service.impl;

import com.ao.dto.AppelOffre;
import com.ao.service.AppelOffreIngestionService;
import com.ao.service.ScraperDetailService;
import com.ao.service.ScraperService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScraperJobServiceImplTest {

    @Mock
    private ScraperService listService;

    @Mock
    private ScraperDetailService detailService;

    @Mock
    private AppelOffreIngestionService ingestionService;

    @InjectMocks
    private ScraperJobServiceImpl service;

    @Test
    void shouldEnrichAndIngestEachAo() {
        AppelOffre ao1 = AppelOffre.builder().reference("AO-1").build();
        AppelOffre ao2 = AppelOffre.builder().reference("AO-2").build();

        when(listService.fetchAll()).thenReturn(List.of(ao1, ao2));
        when(detailService.enrich(ao1)).thenReturn(ao1);
        when(detailService.enrich(ao2)).thenReturn(ao2);

        service.run();

        verify(detailService, times(1)).enrich(ao1);
        verify(detailService, times(1)).enrich(ao2);
        verify(ingestionService, times(1)).ingestIfNew(ao1);
        verify(ingestionService, times(1)).ingestIfNew(ao2);
    }

    @Test
    void shouldContinueWhenOneAoFails() {
        AppelOffre ao1 = AppelOffre.builder().reference("AO-1").build();
        AppelOffre ao2 = AppelOffre.builder().reference("AO-2").build();

        when(listService.fetchAll()).thenReturn(List.of(ao1, ao2));
        when(detailService.enrich(ao1)).thenThrow(new RuntimeException("boom"));
        when(detailService.enrich(ao2)).thenReturn(ao2);

        service.run();

        verify(detailService, times(1)).enrich(ao1);
        verify(detailService, times(1)).enrich(ao2);
        verify(ingestionService, times(1)).ingestIfNew(ao2);
        verify(ingestionService, times(0)).ingestIfNew(ao1);
        verify(ingestionService, times(1)).ingestIfNew(any());
    }
}
