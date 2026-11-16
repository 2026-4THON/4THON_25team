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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowRecommendationService {

    private static final double HALF_UNITS_PER_STAR = 2.0;
    private static final int MAX_RANKS = 100;

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

    public List<ShowQuery.ExploreShow> findExploreShows(LocalDate today, int budget, Set<LocalDate> dates) {
        if (dates.isEmpty()) {
            return List.of();
        }
        List<ShowCandidateRepository.ExploreRow> rows = showCandidateRepository.findExploreCandidates(today, budget);
        if (rows.isEmpty()) {
            return List.of();
        }
        List<Long> performanceIds = rows.stream().map(ShowCandidateRepository.ExploreRow::getPerformanceId).toList();
        Map<Long, SortedSet<LocalDate>> availableDates = new HashMap<>();
        showCandidateRepository.findSessions(
                        performanceIds,
                        Collections.min(dates).atStartOfDay(),
                        Collections.max(dates).plusDays(1).atStartOfDay()
                )
                .forEach(session -> {
                    LocalDate date = session.getStartsAt().toLocalDate();
                    if (dates.contains(date)) {
                        availableDates.computeIfAbsent(session.getPerformanceId(), id -> new TreeSet<>()).add(date);
                    }
                });
        Map<Long, Map<TasteTag, Double>> tagScores = showTagScoreService.findScores(availableDates.keySet());
        List<ShowQuery.ExploreShow> shows = new ArrayList<>();
        for (ShowCandidateRepository.ExploreRow row : rows) {
            SortedSet<LocalDate> matched = availableDates.get(row.getPerformanceId());
            if (matched == null) {
                continue;
            }
            shows.add(new ShowQuery.ExploreShow(
                    new ShowQuery.Candidate(
                            row.getPerformanceId(),
                            row.getTitle(),
                            row.getGenre().name(),
                            row.getPosterUrl(),
                            row.getStartDate(),
                            row.getEndDate(),
                            tagScores.getOrDefault(row.getPerformanceId(), Map.of())
                    ),
                    row.getMinPrice(),
                    List.copyOf(matched),
                    row.getSynopsis()
            ));
        }
        return shows;
    }

    public Map<Long, Integer> findLatestRankMap() {
        LocalDate rankedDate = showCandidateRepository.findLatestRankedDate();
        if (rankedDate == null) {
            return Map.of();
        }
        Map<Long, Integer> ranks = new HashMap<>();
        showCandidateRepository.findRanked(rankedDate, null, PageRequest.of(0, MAX_RANKS))
                .forEach(row -> ranks.putIfAbsent(row.getPerformanceId(), row.getRankNo()));
        return ranks;
    }

    public Map<Long, Long> findReviewCounts(Collection<Long> performanceIds) {
        if (performanceIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> counts = new HashMap<>();
        showRatingRepository.findReviewCounts(performanceIds)
                .forEach(count -> counts.put(count.getPerformanceId(), count.getReviewCount()));
        return counts;
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
