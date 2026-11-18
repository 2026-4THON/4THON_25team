package com.fourthon.backend.recommendation.domain;

import java.util.List;

public record GeneratedReason(
        Long performanceId,
        String reason,
        List<Long> citationIds
) {
}
