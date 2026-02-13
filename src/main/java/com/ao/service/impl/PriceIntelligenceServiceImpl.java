package com.ao.service.impl;

import com.ao.dto.PriceEstimateRequest;
import com.ao.dto.PriceEstimateResponse;
import com.ao.entity.AppelOffreEntity;
import com.ao.repository.AppelOffreRepository;
import com.ao.service.PriceIntelligenceService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PriceIntelligenceServiceImpl implements PriceIntelligenceService {

    private final AppelOffreRepository appelOffreRepository;

    @Override
    public PriceEstimateResponse estimate(PriceEstimateRequest request) {
        List<String> features = new ArrayList<>();

        Specification<AppelOffreEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNotNull(root.get("budgetEstime")));

            if (hasText(request.getDomaine())) {
                predicates.add(cb.like(cb.lower(root.get("domaine")), like(request.getDomaine())));
                features.add("domaine");
            }
            if (hasText(request.getTypeMarche())) {
                predicates.add(cb.like(cb.lower(root.get("typeMarche")), like(request.getTypeMarche())));
                features.add("typeMarche");
            }
            if (hasText(request.getRegion())) {
                predicates.add(cb.like(cb.lower(root.get("lieuExec")), like(request.getRegion())));
                features.add("region");
            }
            if (hasText(request.getOrganisme())) {
                predicates.add(cb.like(cb.lower(root.get("organisme")), like(request.getOrganisme())));
                features.add("organisme");
            }
            if (request.getPublicationFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("datePublication"), request.getPublicationFrom()));
                features.add("publicationFrom");
            }
            if (request.getPublicationTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("datePublication"), request.getPublicationTo()));
                features.add("publicationTo");
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<BigDecimal> budgets = appelOffreRepository.findAll(spec).stream()
                .map(AppelOffreEntity::getBudgetEstime)
                .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        if (budgets.isEmpty()) {
            return PriceEstimateResponse.builder()
                    .sampleSize(0)
                    .featuresUsed(features)
                    .build();
        }

        BigDecimal min = budgets.get(0);
        BigDecimal max = budgets.get(budgets.size() - 1);
        BigDecimal avg = budgets.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(budgets.size()), 2, RoundingMode.HALF_UP);
        BigDecimal median = median(budgets);

        BigDecimal suggested = median;
        if (request.getBudgetHint() != null && request.getBudgetHint().compareTo(BigDecimal.ZERO) > 0) {
            suggested = median.add(request.getBudgetHint()).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            features.add("budgetHint");
        }

        return PriceEstimateResponse.builder()
                .sampleSize(budgets.size())
                .minBudget(min)
                .maxBudget(max)
                .averageBudget(avg)
                .medianBudget(median)
                .suggestedBudget(suggested)
                .featuresUsed(features.stream().distinct().toList())
                .build();
    }

    private BigDecimal median(List<BigDecimal> sortedBudgets) {
        int size = sortedBudgets.size();
        if (size % 2 == 1) {
            return sortedBudgets.get(size / 2);
        }
        BigDecimal a = sortedBudgets.get((size / 2) - 1);
        BigDecimal b = sortedBudgets.get(size / 2);
        return a.add(b).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String like(String value) {
        return "%" + value.trim().toLowerCase() + "%";
    }
}
