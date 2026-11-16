package com.fourthon.backend.ai.service;

import com.fourthon.backend.ai.dto.ReviewAnalysisResult;
import com.fourthon.backend.ai.repository.ReviewTagScoreRepository;
import com.fourthon.backend.common.domain.TasteTag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewTagScoreService {

    private final ReviewTagScoreRepository reviewTagScoreRepository;

    public ReviewAnalysisResult.TagSummary summarize(Long performanceId) {
        long reviewCount = reviewTagScoreRepository.countReviews(performanceId);
        Map<TasteTag, Double> averages = new EnumMap<>(TasteTag.class);
        reviewTagScoreRepository.findAverages(performanceId)
                .forEach(average -> averages.put(average.getTag(), average.getAverage()));
        return new ReviewAnalysisResult.TagSummary(reviewCount, averages);
    }
}
