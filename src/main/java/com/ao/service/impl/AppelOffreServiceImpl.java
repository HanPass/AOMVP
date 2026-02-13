package com.ao.service.impl;

import com.ao.dto.AppelOffre;
import com.ao.dto.AppelOffreSearchCriteria;
import com.ao.mapper.AppelOffreMapper;
import com.ao.repository.AppelOffreRepository;
import com.ao.service.AppelOffreService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppelOffreServiceImpl implements AppelOffreService {

    private final AppelOffreRepository appelOffreRepository;

    @Override
    public List<AppelOffre> getAllAppelOffre() {
        return AppelOffreMapper.toDtos(appelOffreRepository.findAll());
    }

    @Override
    public Page<AppelOffre> search(AppelOffreSearchCriteria criteria, Pageable pageable) {
        Specification<com.ao.entity.AppelOffreEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(criteria.getRegion())) {
                predicates.add(cb.like(cb.lower(root.get("lieuExec")), like(criteria.getRegion())));
            }
            if (hasText(criteria.getDomaine())) {
                predicates.add(cb.like(cb.lower(root.get("domaine")), like(criteria.getDomaine())));
            }
            if (hasText(criteria.getOrganisme())) {
                predicates.add(cb.like(cb.lower(root.get("organisme")), like(criteria.getOrganisme())));
            }
            if (hasText(criteria.getTypeMarche())) {
                predicates.add(cb.like(cb.lower(root.get("typeMarche")), like(criteria.getTypeMarche())));
            }
            if (criteria.getBudgetMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("budgetEstime"), criteria.getBudgetMin()));
            }
            if (criteria.getBudgetMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("budgetEstime"), criteria.getBudgetMax()));
            }
            if (criteria.getDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("datePublication"), criteria.getDateFrom()));
            }
            if (criteria.getDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("datePublication"), criteria.getDateTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return appelOffreRepository.findAll(specification, pageable)
                .map(AppelOffreMapper::toDto);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String like(String value) {
        return "%" + value.trim().toLowerCase() + "%";
    }
}
