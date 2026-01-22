package com.ao.service.impl;

import com.ao.dto.TenderDto;
import com.ao.entity.Tender;
import com.ao.repository.TenderRepository;
import com.ao.service.TenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenderServiceImpl implements TenderService {

    private final TenderRepository tenderRepository;

    @Override
    public int saveIfNew(List<TenderDto> dtos) {
        int inserted = 0;

        for (TenderDto dto : dtos) {

            if (tenderRepository.existsBySourceId(dto.sourceId())) {
                continue;
            }

            Tender tender = Tender.builder()
                    .sourceId(dto.sourceId())
                    .reference(dto.reference())
                    .title(dto.title())
                    .organisme(dto.organisme())
                    .region(dto.region())
                    .deadline(dto.deadline())
                    .url(dto.url())
                    .build();

            tenderRepository.save(tender);
            inserted++;
        }

        log.info("AO insérés: {}", inserted);
        return inserted;
    }
}
