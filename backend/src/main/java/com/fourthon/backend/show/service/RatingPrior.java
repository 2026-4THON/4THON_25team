package com.fourthon.backend.show.service;

import com.fourthon.backend.show.config.RatingProperties;
import com.fourthon.backend.show.repository.ShowRatingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RatingPrior {

    private static final double HALF_UNITS_PER_STAR = 2.0;

    private final ShowRatingRepository showRatingRepository;
    private final RatingProperties ratingProperties;

    private volatile double priorCount;
    private volatile double globalMean;
    private volatile boolean loaded;

    @EventListener(ApplicationReadyEvent.class)
    public void refresh() {
        List<Long> counts = showRatingRepository.findRatingCountsPerShow();
        Double averageHalfUnits = showRatingRepository.findAverageHalfUnits();
        priorCount = Math.max(median(counts), ratingProperties.minPriorCount());
        globalMean = averageHalfUnits == null
                ? ratingProperties.defaultMean()
                : averageHalfUnits / HALF_UNITS_PER_STAR;
        loaded = true;
        log.info("평점 보정값 갱신: m={}, 전체 평균={}", priorCount, globalMean);
    }

    public double adjusted(long ratingCount, Double average) {
        if (!loaded) {
            refresh();
        }
        if (ratingCount == 0 || average == null) {
            return globalMean;
        }
        return (ratingCount * average + priorCount * globalMean) / (ratingCount + priorCount);
    }

    private double median(List<Long> sortedCounts) {
        if (sortedCounts.isEmpty()) {
            return 0;
        }
        int middle = sortedCounts.size() / 2;
        if (sortedCounts.size() % 2 == 1) {
            return sortedCounts.get(middle);
        }
        return (sortedCounts.get(middle - 1) + sortedCounts.get(middle)) / 2.0;
    }
}
