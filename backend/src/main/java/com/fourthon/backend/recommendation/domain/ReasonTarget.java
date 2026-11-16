package com.fourthon.backend.recommendation.domain;

import com.fourthon.backend.ai.dto.ReviewAnalysisResult;

import java.util.List;

public record ReasonTarget(
        Long performanceId,
        String title,
        String ground,
        List<ReviewAnalysisResult.Evidence> reviews
) {
}
