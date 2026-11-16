package com.fourthon.backend.recommendation.service;

import com.fourthon.backend.common.domain.TasteTag;
import com.fourthon.backend.recommendation.config.RecommendationProperties;
import com.fourthon.backend.recommendation.domain.GenreTab;
import com.fourthon.backend.recommendation.domain.HomePicks;
import com.fourthon.backend.recommendation.domain.HomeScope;
import com.fourthon.backend.recommendation.domain.RecommendationReason;
import com.fourthon.backend.recommendation.dto.RecommendationResponse;
import com.fourthon.backend.recommendation.repository.RecommendationReasonRepository;
import com.fourthon.backend.recommendation.score.FactorType;
import com.fourthon.backend.recommendation.score.RecommendationContext;
import com.fourthon.backend.recommendation.score.ScoreCalculator;
import com.fourthon.backend.recommendation.score.ScoredShow;
import com.fourthon.backend.show.domain.ShowStatus;
import com.fourthon.backend.show.dto.ShowQuery;
import com.fourthon.backend.show.service.ShowRecommendationService;
import com.fourthon.backend.taste.service.LifeWorkPrecedentService;
import com.fourthon.backend.taste.service.TasteService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeRecommendationService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final TasteService tasteService;
    private final LifeWorkPrecedentService lifeWorkPrecedentService;
    private final ShowRecommendationService showRecommendationService;
    private final ScoreCalculator scoreCalculator;
    private final HomeSelector homeSelector;
    private final ReasonGenerator reasonGenerator;
    private final RecommendationReasonRepository recommendationReasonRepository;
    private final RecommendationProperties recommendationProperties;

    public RecommendationResponse.Home recommend(Long userId, GenreTab genreTab, HomeScope scope) {
        HomePicks picks = pick(userId, genreTab, scope);
        if (!picks.hasReport()) {
            return new RecommendationResponse.Home(false, null, List.of());
        }
        LocalDate today = LocalDate.now(SEOUL);
        Map<Long, String> savedReasons = findSavedReasons(userId, picks.shows());
        List<RecommendationResponse.HomeItem> items = picks.shows().stream()
                .map(show -> toItem(show, today, savedReasons.getOrDefault(
                        show.candidate().performanceId(),
                        reasonGenerator.homeReason(show, picks.weights(), picks.tasteScores(), picks.lifeWorkTitle())
                )))
                .toList();
        return new RecommendationResponse.Home(true, picks.briefing(), items);
    }

    public HomePicks pick(Long userId, GenreTab genreTab, HomeScope scope) {
        Optional<String> briefing = tasteService.findBriefing(userId);
        if (briefing.isEmpty()) {
            return HomePicks.noReport();
        }
        RecommendationProperties.Home home = recommendationProperties.home();
        Map<FactorType, Double> weights = home.weights().toMap();
        Map<TasteTag, Double> tasteScores = tasteService.findScores(userId);
        Optional<String> lifeWorkTitle = tasteService.findLifeWorkTitles(userId).stream().findFirst();
        List<ShowQuery.Candidate> candidates = showRecommendationService.findCandidates(
                genreTab.toGenre(),
                scope == HomeScope.ONGOING,
                LocalDate.now(SEOUL),
                userId
        );
        if (candidates.isEmpty()) {
            return new HomePicks(true, briefing.get(), tasteScores, weights, lifeWorkTitle, List.of());
        }
        Set<Long> performanceIds = candidates.stream()
                .map(ShowQuery.Candidate::performanceId)
                .collect(Collectors.toSet());
        RecommendationContext context = RecommendationContext.forHome(
                tasteScores,
                lifeWorkPrecedentService.findScores(userId, performanceIds),
                showRecommendationService.findAdjustedRatings(performanceIds)
        );
        List<ScoredShow> passed = candidates.stream()
                .map(candidate -> scoreCalculator.score(context, candidate, weights))
                .filter(show -> passes(show, home.minMatch()))
                .sorted(ranking())
                .toList();
        List<ScoredShow> picked = homeSelector.select(passed, home.size(), home.accurateCount());
        return new HomePicks(true, briefing.get(), tasteScores, weights, lifeWorkTitle, picked);
    }

    private Map<Long, String> findSavedReasons(Long userId, List<ScoredShow> shows) {
        if (shows.isEmpty()) {
            return Map.of();
        }
        return recommendationReasonRepository.findByUserIdAndPerformanceIdIn(userId, shows.stream()
                        .map(show -> show.candidate().performanceId())
                        .toList())
                .stream()
                .filter(reason -> reasonGenerator.fits(reason.getReason()))
                .collect(Collectors.toMap(RecommendationReason::getPerformanceId, RecommendationReason::getReason));
    }

    private boolean passes(ScoredShow show, double minMatch) {
        return show.factor(FactorType.GENRE) >= minMatch
                || show.factor(FactorType.MOOD) >= minMatch
                || show.factor(FactorType.PRECEDENT) > 0.0;
    }

    private Comparator<ScoredShow> ranking() {
        return Comparator.comparingDouble(ScoredShow::total).reversed()
                .thenComparing(Comparator.comparingDouble((ScoredShow show) -> show.factor(FactorType.RATING)).reversed())
                .thenComparing(
                        (ScoredShow show) -> show.candidate().startDate(),
                        Comparator.nullsLast(Comparator.<LocalDate>reverseOrder())
                );
    }

    private RecommendationResponse.HomeItem toItem(ScoredShow show, LocalDate today, String reason) {
        ShowQuery.Candidate candidate = show.candidate();
        return new RecommendationResponse.HomeItem(
                candidate.performanceId(),
                candidate.title(),
                candidate.genre(),
                candidate.posterUrl(),
                ShowStatus.of(candidate.startDate(), candidate.endDate(), today).name(),
                candidate.startDate(),
                candidate.endDate(),
                reason
        );
    }
}
