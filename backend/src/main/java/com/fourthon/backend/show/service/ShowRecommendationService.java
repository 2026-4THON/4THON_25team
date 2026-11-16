package com.fourthon.backend.show.service;

import com.fourthon.backend.common.domain.TasteTag;
import com.fourthon.backend.show.dto.ShowQuery;
import com.fourthon.backend.show.repository.ShowCandidateRepository;
import com.fourthon.backend.show.repository.ShowRatingRepository;
import com.fourthon.backend.ticket.domain.CatalogPerformance;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowRecommendationService {

    private static final double HALF_UNITS_PER_STAR = 2.0;

    private final ShowCandidateRepository showCandidateRepository;
    private final ShowRatingRepository showRatingRepository;
    private final ShowTagScoreService showTagScoreService;
    private final RatingPrior ratingPrior;

    public List<ShowQuery.Candidate> findCandidates(
            CatalogPerformance.Genre genre,
            boolean ongoingOnly,
            LocalDate today,
            Long userId) {

        List<ShowCandidateRepository.ShowRow> rows =
                showCandidateRepository.findCandidates(genre, ongoingOnly, today, userId);
        Map<Long, Map<TasteTag, Double>> tagScores = showTagScoreService.findScores(rows.stream()
                .map(ShowCandidateRepository.ShowRow::getPerformanceId)
                .toList());
        return rows.stream()
                .filter(row -> tagScores.containsKey(row.getPerformanceId()))
                .map(row -> new ShowQuery.Candidate(
                        row.getPerformanceId(),
                        row.getTitle(),
                        row.getGenre().name(),
                        row.getPosterUrl(),
                        row.getStartDate(),
                        row.getEndDate(),
                        tagScores.get(row.getPerformanceId())
                ))
                .toList();
    }

    public Map<Long, Double> findAdjustedRatings(Collection<Long> performanceIds) {
        Map<Long, Double> ratings = new HashMap<>();
        if (performanceIds.isEmpty()) {
            return ratings;
        }
        Map<Long, ShowRatingRepository.PerformanceRating> summaries = new HashMap<>();
        showRatingRepository.findSummaries(performanceIds)
                .forEach(summary -> summaries.put(summary.getPerformanceId(), summary));
        for (Long performanceId : performanceIds) {
            ShowRatingRepository.PerformanceRating summary = summaries.get(performanceId);
            ratings.put(performanceId, summary == null
                    ? ratingPrior.adjusted(0, null)
                    : ratingPrior.adjusted(summary.getRatingCount(), toStars(summary.getAverageHalfUnits())));
        }
        return ratings;
    }

    public List<ShowQuery.RankedShow> findLatestRanks(CatalogPerformance.Genre genre, int limit) {
        LocalDate rankedDate = showCandidateRepository.findLatestRankedDate();
        if (rankedDate == null) {
            return List.of();
        }
        return showCandidateRepository.findRanked(rankedDate, genre, PageRequest.of(0, limit)).stream()
                .map(row -> new ShowQuery.RankedShow(
                        row.getRankNo(),
                        row.getPerformanceId(),
                        row.getTitle(),
                        row.getGenre().name(),
                        row.getPosterUrl(),
                        row.getStartDate(),
                        row.getEndDate()
                ))
                .toList();
    }

    private Double toStars(Double averageHalfUnits) {
        return averageHalfUnits == null ? null : averageHalfUnits / HALF_UNITS_PER_STAR;
    }
}
