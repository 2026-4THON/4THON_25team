package com.fourthon.backend.show.service;

import com.fourthon.backend.ai.dto.ReviewAnalysisResult;
import com.fourthon.backend.ai.service.ReviewTagScoreService;
import com.fourthon.backend.show.domain.ShowTagScore;
import com.fourthon.backend.show.repository.ShowTagScoreRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ShowTagScoreService {

    private final ShowTagScoreRepository showTagScoreRepository;
    private final ReviewTagScoreService reviewTagScoreService;
    private final ShowTagScoreCalculator showTagScoreCalculator;

    public void refreshReviewScores(Collection<Long> performanceIds) {
        performanceIds.forEach(this::refreshReviewScore);
    }

    private void refreshReviewScore(Long performanceId) {
        List<ShowTagScore> scores = showTagScoreRepository.findByPerformanceId(performanceId);
        if (scores.isEmpty()) {
            return;
        }
        ReviewAnalysisResult.TagSummary summary = reviewTagScoreService.summarize(performanceId);
        double weight = showTagScoreCalculator.reviewWeight(summary.reviewCount());
        scores.forEach(score -> score.applyReviewScore(
                summary.averages().getOrDefault(score.getTag(), 0.0),
                weight
        ));
    }
}
