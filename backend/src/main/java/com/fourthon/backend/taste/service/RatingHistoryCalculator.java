package com.fourthon.backend.taste.service;

import com.fourthon.backend.common.domain.TagScores;
import com.fourthon.backend.common.domain.TasteTag;
import com.fourthon.backend.taste.domain.RatingBaseline;
import com.fourthon.backend.taste.domain.TicketRating;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RatingHistoryCalculator {

    public Map<TasteTag, Double> historyScores(
            List<TicketRating> ratings,
            Map<Long, Map<TasteTag, Double>> showTagScores) {

        RatingBaseline baseline = RatingBaseline.of(ratings.stream().map(TicketRating::rating).toList());
        Map<TasteTag, Double> sums = new EnumMap<>(TasteTag.class);
        double weightSum = 0.0;
        for (TicketRating rating : ratings) {
            Map<TasteTag, Double> showScores = showTagScores.get(rating.performanceId());
            double deviation = baseline.deviation(rating.rating());
            if (showScores == null || showScores.isEmpty() || deviation == 0.0) {
                continue;
            }
            weightSum += Math.abs(deviation);
            for (TasteTag tag : TasteTag.values()) {
                sums.merge(tag, deviation * showScores.getOrDefault(tag, 0.0), Double::sum);
            }
        }
        if (weightSum == 0.0) {
            return Map.of();
        }
        Map<TasteTag, Double> scores = new EnumMap<>(TasteTag.class);
        for (TasteTag tag : TasteTag.values()) {
            scores.put(tag, TagScores.clamp(sums.getOrDefault(tag, 0.0) / weightSum));
        }
        return scores;
    }

    public Map<Long, Double> precedentScores(
            int peerCount,
            Collection<TicketRating> peerRatings,
            Collection<Long> performanceIds) {

        if (peerCount == 0) {
            return Map.of();
        }
        Map<Long, List<TicketRating>> ratingsByPeer = peerRatings.stream()
                .collect(Collectors.groupingBy(TicketRating::userId));
        Map<Long, Integer> highCounts = new HashMap<>();
        ratingsByPeer.values().forEach(ratings -> countHighRatings(ratings, performanceIds, highCounts));
        Map<Long, Double> scores = new HashMap<>();
        highCounts.forEach((performanceId, count) -> scores.put(performanceId, (double) count / peerCount));
        return scores;
    }

    private void countHighRatings(
            List<TicketRating> ratings,
            Collection<Long> performanceIds,
            Map<Long, Integer> highCounts) {

        RatingBaseline baseline = RatingBaseline.of(ratings.stream().map(TicketRating::rating).toList());
        ratings.stream()
                .filter(rating -> performanceIds.contains(rating.performanceId()))
                .collect(Collectors.groupingBy(
                        TicketRating::performanceId,
                        Collectors.averagingDouble(TicketRating::rating)
                ))
                .forEach((performanceId, average) -> {
                    if (baseline.isHigh(average)) {
                        highCounts.merge(performanceId, 1, Integer::sum);
                    }
                });
    }
}
