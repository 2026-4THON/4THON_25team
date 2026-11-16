package com.fourthon.backend.recommendation.service;

import com.fourthon.backend.common.domain.TagType;
import com.fourthon.backend.common.domain.TasteTag;
import com.fourthon.backend.common.exception.CodedException;
import com.fourthon.backend.recommendation.config.RecommendationProperties;
import com.fourthon.backend.recommendation.domain.ExploreMode;
import com.fourthon.backend.recommendation.domain.ExploreStatus;
import com.fourthon.backend.recommendation.domain.WishJudgement;
import com.fourthon.backend.recommendation.dto.RecommendationRequest;
import com.fourthon.backend.recommendation.dto.RecommendationResponse;
import com.fourthon.backend.recommendation.score.FactorType;
import com.fourthon.backend.recommendation.score.RecommendationContext;
import com.fourthon.backend.recommendation.score.ScoreCalculator;
import com.fourthon.backend.recommendation.score.ScoredShow;
import com.fourthon.backend.show.dto.ShowQuery;
import com.fourthon.backend.show.service.ShowRecommendationService;
import com.fourthon.backend.taste.service.TasteService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExploreService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final String ANY_SUMMARY = "조건에 맞는 가장 인기있는 공연을 추천해드려요";
    private static final String NEW_SUMMARY_SUFFIX = " 작품을 추천해드려요";
    private static final int SUMMARY_TAG_COUNT = 3;
    private static final Map<String, String> COMPANION_LABELS = Map.of(
            "ALONE", "혼자",
            "PARTNER", "연인",
            "FRIEND", "친구",
            "FAMILY", "가족"
    );

    private final TasteService tasteService;
    private final ShowRecommendationService showRecommendationService;
    private final ScoreCalculator scoreCalculator;
    private final WishJudge wishJudge;
    private final RecommendationProperties recommendationProperties;

    public RecommendationResponse.Explore explore(Long userId, RecommendationRequest.Explore request) {
        Optional<String> briefing = tasteService.findBriefing(userId);
        if (briefing.isEmpty()) {
            throw new CodedException(HttpStatus.NOT_FOUND, "TASTE_REPORT_NOT_FOUND", "취향 리포트가 없습니다");
        }
        Set<TasteTag> selectedTags = request.mode() == ExploreMode.NEW ? selectedTags(request) : Set.of();
        List<ShowQuery.ExploreShow> shows = showRecommendationService.findExploreShows(
                LocalDate.now(SEOUL),
                request.budget(),
                new HashSet<>(request.dates())
        );
        if (shows.isEmpty()) {
            return noMatch();
        }
        Set<Long> performanceIds = shows.stream()
                .map(show -> show.candidate().performanceId())
                .collect(Collectors.toSet());
        Map<Long, Double> ratings = showRecommendationService.findAdjustedRatings(performanceIds);
        Map<Long, Integer> ranks = showRecommendationService.findLatestRankMap();
        if (request.mode() == ExploreMode.ANY) {
            return exploreAny(shows, ratings, ranks);
        }
        return exploreByTaste(userId, request, briefing.get(), selectedTags, shows, ratings, ranks);
    }

    private RecommendationResponse.Explore exploreByTaste(
            Long userId,
            RecommendationRequest.Explore request,
            String briefing,
            Set<TasteTag> selectedTags,
            List<ShowQuery.ExploreShow> shows,
            Map<Long, Double> ratings,
            Map<Long, Integer> ranks) {

        RecommendationProperties.Explore explore = recommendationProperties.explore();
        Map<TasteTag, Double> tasteScores = request.mode() == ExploreMode.REPORT ? tasteService.findScores(userId) : Map.of();
        Map<Long, Double> popularity = popularityScores(ranks, explore.popularityRankCount());
        Map<FactorType, Double> baseWeights = explore.weights().toMap(false);
        RecommendationContext baseContext = RecommendationContext.forExplore(
                tasteScores, selectedTags, ratings, popularity, Map.of());
        Map<Long, ShowQuery.ExploreShow> showById = shows.stream()
                .collect(Collectors.toMap(show -> show.candidate().performanceId(), Function.identity()));
        List<ScoredShow> passed = shows.stream()
                .map(show -> scoreCalculator.score(baseContext, show.candidate(), baseWeights))
                .filter(show -> show.factor(FactorType.GENRE) >= explore.minMatch()
                        || show.factor(FactorType.MOOD) >= explore.minMatch())
                .toList();
        if (passed.isEmpty()) {
            return noMatch();
        }
        List<ScoredShow> llmTargets = passed.stream()
                .sorted(Comparator.comparingDouble((ScoredShow show) -> tasteMatch(show, baseWeights)).reversed())
                .limit(explore.maxLlmCandidates())
                .toList();
        boolean hasWish = request.wish() != null && !request.wish().isBlank();
        boolean needSummary = request.mode() == ExploreMode.NEW;
        WishJudgement judgement = hasWish || needSummary
                ? wishJudge.judge(
                        COMPANION_LABELS.getOrDefault(request.companion().name(), request.companion().name()),
                        request.wish(),
                        selectedTags,
                        needSummary,
                        llmTargets.stream().map(show -> showById.get(show.candidate().performanceId())).toList())
                : WishJudgement.empty();
        boolean useWish = hasWish && judgement.hasScores();
        Map<FactorType, Double> weights = explore.weights().toMap(useWish);
        RecommendationContext context = RecommendationContext.forExplore(
                tasteScores, selectedTags, ratings, popularity, useWish ? judgement.scores() : Map.of());
        List<RecommendationResponse.ExploreItem> items = passed.stream()
                .map(show -> scoreCalculator.score(context, show.candidate(), weights))
                .sorted(ranking())
                .limit(explore.size())
                .map(show -> toItem(showById.get(show.candidate().performanceId())))
                .toList();
        String summary = request.mode() == ExploreMode.REPORT
                ? briefing
                : judgement.summary().orElseGet(() -> newSummary(selectedTags));
        return new RecommendationResponse.Explore(ExploreStatus.OK.name(), summary, items);
    }

    private RecommendationResponse.Explore exploreAny(
            List<ShowQuery.ExploreShow> shows,
            Map<Long, Double> ratings,
            Map<Long, Integer> ranks) {

        Map<Long, Long> reviewCounts = showRecommendationService.findReviewCounts(shows.stream()
                .map(show -> show.candidate().performanceId())
                .toList());
        Comparator<ShowQuery.ExploreShow> order = Comparator
                .comparingInt((ShowQuery.ExploreShow show) ->
                        ranks.getOrDefault(show.candidate().performanceId(), Integer.MAX_VALUE))
                .thenComparing(Comparator.comparingLong((ShowQuery.ExploreShow show) ->
                        reviewCounts.getOrDefault(show.candidate().performanceId(), 0L)).reversed())
                .thenComparing(Comparator.comparingDouble((ShowQuery.ExploreShow show) ->
                        ratings.getOrDefault(show.candidate().performanceId(), 0.0)).reversed());
        List<RecommendationResponse.ExploreItem> items = shows.stream()
                .sorted(order)
                .limit(recommendationProperties.explore().size())
                .map(this::toItem)
                .toList();
        return new RecommendationResponse.Explore(ExploreStatus.OK.name(), ANY_SUMMARY, items);
    }

    private Set<TasteTag> selectedTags(RecommendationRequest.Explore request) {
        Set<TasteTag> tags = EnumSet.noneOf(TasteTag.class);
        if (request.genres() != null) {
            request.genres().forEach(label -> tags.add(toTag(label, TagType.GENRE)));
        }
        if (request.moods() != null) {
            request.moods().forEach(label -> tags.add(toTag(label, TagType.MOOD)));
        }
        if (tags.isEmpty()) {
            throw new CodedException(HttpStatus.BAD_REQUEST, "TAG_REQUIRED", "새로 설정 모드는 장르나 분위기를 하나 이상 골라야 합니다");
        }
        return tags;
    }

    private TasteTag toTag(String label, TagType type) {
        return TasteTag.fromLabel(label, type)
                .orElseThrow(() -> new CodedException(HttpStatus.BAD_REQUEST, "UNKNOWN_TAG",
                        "알 수 없는 " + (type == TagType.GENRE ? "장르" : "분위기") + "입니다: " + label));
    }

    private Map<Long, Double> popularityScores(Map<Long, Integer> ranks, int rankCount) {
        Map<Long, Double> scores = new HashMap<>();
        ranks.forEach((performanceId, rank) -> {
            if (rank >= 1 && rank <= rankCount) {
                scores.put(performanceId, (rankCount + 1 - rank) / (double) rankCount);
            }
        });
        return scores;
    }

    private double tasteMatch(ScoredShow show, Map<FactorType, Double> weights) {
        return weights.getOrDefault(FactorType.GENRE, 0.0) * show.factor(FactorType.GENRE)
                + weights.getOrDefault(FactorType.MOOD, 0.0) * show.factor(FactorType.MOOD);
    }

    private Comparator<ScoredShow> ranking() {
        return Comparator.comparingDouble(ScoredShow::total).reversed()
                .thenComparing(Comparator.comparingDouble((ScoredShow show) -> show.factor(FactorType.RATING)).reversed())
                .thenComparing(
                        (ScoredShow show) -> show.candidate().startDate(),
                        Comparator.nullsLast(Comparator.<LocalDate>reverseOrder())
                );
    }

    private String newSummary(Set<TasteTag> selectedTags) {
        return selectedTags.stream()
                .limit(SUMMARY_TAG_COUNT)
                .map(TasteTag::getLabel)
                .collect(Collectors.joining("·")) + NEW_SUMMARY_SUFFIX;
    }

    private RecommendationResponse.Explore noMatch() {
        return new RecommendationResponse.Explore(ExploreStatus.NO_MATCH.name(), null, List.of());
    }

    private RecommendationResponse.ExploreItem toItem(ShowQuery.ExploreShow show) {
        ShowQuery.Candidate candidate = show.candidate();
        return new RecommendationResponse.ExploreItem(
                candidate.performanceId(),
                candidate.title(),
                candidate.genre(),
                candidate.posterUrl(),
                candidate.startDate(),
                candidate.endDate(),
                show.minPrice(),
                show.availableDates()
        );
    }
}
