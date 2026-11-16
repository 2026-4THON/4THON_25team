package com.fourthon.backend.recommendation.service;

import com.fourthon.backend.common.domain.TasteTag;
import com.fourthon.backend.recommendation.config.RecommendationProperties;
import com.fourthon.backend.recommendation.domain.GenreTab;
import com.fourthon.backend.recommendation.domain.HomeScope;
import com.fourthon.backend.recommendation.dto.RecommendationResponse;
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
    private final RecommendationProperties recommendationProperties;

    public RecommendationResponse.Home recommend(Long userId, GenreTab genreTab, HomeScope scope) {
        Optional<String> briefing = tasteService.findBriefing(userId);
        if (briefing.isEmpty()) {
            return new RecommendationResponse.Home(false, null, List.of());
        }
        LocalDate today = LocalDate.now(SEOUL);
        List<ShowQuery.Candidate> candidates = showRecommendationService.findCandidates(
                genreTab.toGenre(),
                scope == HomeScope.ONGOING,
                today,
                userId
        );
        if (candidates.isEmpty()) {
            return new RecommendationResponse.Home(true, briefing.get(), List.of());
        }
        Set<Long> performanceIds = candidates.stream()
                .map(ShowQuery.Candidate::performanceId)
                .collect(Collectors.toSet());
        Map<TasteTag, Double> tasteScores = tasteService.findScores(userId);
        RecommendationContext context = RecommendationContext.forHome(
                tasteScores,
                lifeWorkPrecedentService.findScores(userId, performanceIds),
                showRecommendationService.findAdjustedRatings(performanceIds)
        );
        RecommendationProperties.Home home = recommendationProperties.home();
        Map<FactorType, Double> weights = home.weights().toMap();
        List<ScoredShow> passed = candidates.stream()
                .map(candidate -> scoreCalculator.score(context, candidate, weights))
                .filter(show -> passes(show, home.minMatch()))
                .sorted(ranking())
                .toList();
        Optional<String> lifeWorkTitle = tasteService.findLifeWorkTitles(userId).stream().findFirst();
        List<RecommendationResponse.HomeItem> items = homeSelector.select(passed, home.size(), home.accurateCount())
                .stream()
                .map(show -> toItem(show, today, reasonGenerator.homeReason(show, weights, tasteScores, lifeWorkTitle)))
                .toList();
        return new RecommendationResponse.Home(true, briefing.get(), items);
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
