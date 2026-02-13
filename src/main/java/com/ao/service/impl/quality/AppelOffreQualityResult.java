package com.ao.service.impl.quality;

import com.ao.dto.AppelOffre;

import java.util.List;

public record AppelOffreQualityResult(
        AppelOffre normalized,
        List<String> issues
) {
    public boolean isValid() {
        return issues == null || issues.isEmpty();
    }
}
