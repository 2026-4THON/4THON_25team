package com.fourthon.backend.taste.service;

import com.fourthon.backend.taste.domain.LifeWork;
import com.fourthon.backend.taste.domain.TicketRating;
import com.fourthon.backend.taste.repository.TasteReportRepository;
import com.fourthon.backend.taste.repository.TicketRatingRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LifeWorkPrecedentService {

    private final TasteReportRepository tasteReportRepository;
    private final TicketRatingRepository ticketRatingRepository;
    private final RatingHistoryCalculator ratingHistoryCalculator;

    public Map<Long, Double> findScores(Long userId, Collection<Long> performanceIds) {
        if (performanceIds.isEmpty()) {
            return Map.of();
        }
        Set<String> titles = tasteReportRepository.findByUserId(userId)
                .map(report -> report.getLifeWorks().stream()
                        .map(LifeWork::getNormalizedTitle)
                        .filter(title -> !title.isEmpty())
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
        if (titles.isEmpty()) {
            return Map.of();
        }
        List<Long> peerIds = tasteReportRepository.findPeerUserIds(titles, userId);
        if (peerIds.isEmpty()) {
            return Map.of();
        }
        List<TicketRating> peerRatings = ticketRatingRepository.findByUserIds(peerIds).stream()
                .map(row -> TicketRating.ofHalfUnits(row.getUserId(), row.getPerformanceId(), row.getRatingHalfUnits()))
                .toList();
        return ratingHistoryCalculator.precedentScores(peerIds.size(), peerRatings, Set.copyOf(performanceIds));
    }
}
