package com.fourthon.backend.recommendation;

import com.fourthon.backend.common.domain.TasteTag;
import com.fourthon.backend.recommendation.config.RecommendationProperties;
import com.fourthon.backend.recommendation.score.FactorType;
import com.fourthon.backend.recommendation.score.GenreFactor;
import com.fourthon.backend.recommendation.score.MoodFactor;
import com.fourthon.backend.recommendation.score.PrecedentFactor;
import com.fourthon.backend.recommendation.score.RatingFactor;
import com.fourthon.backend.recommendation.score.RecommendationContext;
import com.fourthon.backend.recommendation.score.ScoreCalculator;
import com.fourthon.backend.recommendation.score.ScoredShow;
import com.fourthon.backend.recommendation.service.HomeSelector;
import com.fourthon.backend.recommendation.service.ReasonGenerator;
import com.fourthon.backend.show.dto.ShowQuery;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class RecommendationScoreTest {

    private final ScoreCalculator calculator = new ScoreCalculator(List.of(
            new GenreFactor(), new MoodFactor(), new PrecedentFactor(), new RatingFactor()
    ));
    private final RecommendationProperties properties = new RecommendationProperties(
            new RecommendationProperties.Home(5, 3, 0.3,
                    new RecommendationProperties.HomeWeights(0.45, 0.30, 0.15, 0.10)),
            9,
            24
    );
    private final Map<FactorType, Double> weights = properties.home().weights().toMap();
    private final Map<TasteTag, Double> taste = Map.of(
            TasteTag.ROMANCE, 1.0,
            TasteTag.WARM, 1.0
    );

    @Test
    void 장르_분위기_선례_평점을_가중치로_합산한다() {
        ShowQuery.Candidate show = candidate(1L, Map.of(TasteTag.ROMANCE, 0.8, TasteTag.WARM, 0.6));
        RecommendationContext context = RecommendationContext.forHome(
                taste, Map.of(1L, 0.5), Map.of(1L, 4.0)
        );

        ScoredShow scored = calculator.score(context, show, weights);

        assertThat(scored.factor(FactorType.GENRE)).isCloseTo(0.8, within(1e-9));
        assertThat(scored.factor(FactorType.MOOD)).isCloseTo(0.6, within(1e-9));
        assertThat(scored.total()).isCloseTo(0.45 * 0.8 + 0.30 * 0.6 + 0.15 * 0.5 + 0.10 * 0.8, within(1e-9));
    }

    @Test
    void 장르_일치는_내_취향_점수로_가중한_공연_태그_점수_평균이다() {
        Map<TasteTag, Double> mixedTaste = Map.of(TasteTag.ROMANCE, 1.0, TasteTag.HORROR, 0.5);
        ShowQuery.Candidate show = candidate(1L, Map.of(TasteTag.ROMANCE, 0.9, TasteTag.HORROR, 0.0));
        RecommendationContext context = RecommendationContext.forHome(mixedTaste, Map.of(), Map.of());

        ScoredShow scored = calculator.score(context, show, weights);

        assertThat(scored.factor(FactorType.GENRE)).isCloseTo(0.6, within(1e-9));
    }

    @Test
    void 홈_4_5위는_1_3위와_대표_장르가_다른_공연을_먼저_고른다() {
        List<ScoredShow> sorted = List.of(
                scored(1L, TasteTag.ROMANCE, 0.9),
                scored(2L, TasteTag.ROMANCE, 0.8),
                scored(3L, TasteTag.COMEDY, 0.7),
                scored(4L, TasteTag.ROMANCE, 0.6),
                scored(5L, TasteTag.COMEDY, 0.5),
                scored(6L, TasteTag.FANTASY, 0.4),
                scored(7L, TasteTag.HORROR, 0.3)
        );

        List<Long> picked = new HomeSelector().select(sorted, 5, 3).stream()
                .map(show -> show.candidate().performanceId())
                .toList();

        assertThat(picked).containsExactly(1L, 2L, 3L, 6L, 7L);
    }

    @Test
    void 기준을_통과한_공연이_적으면_있는_만큼만_고른다() {
        List<ScoredShow> sorted = List.of(scored(1L, TasteTag.ROMANCE, 0.9), scored(2L, TasteTag.ROMANCE, 0.8));

        assertThat(new HomeSelector().select(sorted, 5, 3)).hasSize(2);
    }

    @Test
    void 추천_이유는_가장_크게_기여한_근거로_만든다() {
        ReasonGenerator generator = new ReasonGenerator(properties);
        ShowQuery.Candidate show = candidate(1L, Map.of(TasteTag.ROMANCE, 0.9, TasteTag.WARM, 0.2));
        ScoredShow scored = calculator.score(
                RecommendationContext.forHome(taste, Map.of(), Map.of()), show, weights);

        assertThat(generator.homeReason(scored, weights, taste, Optional.empty()))
                .isEqualTo("로맨스를 좋아하는 당신에게");
    }

    @Test
    void 선례가_가장_크면_인생작으로_이유를_만들고_길면_대체_문구를_쓴다() {
        ReasonGenerator generator = new ReasonGenerator(properties);
        ShowQuery.Candidate show = candidate(1L, Map.of());
        ScoredShow scored = calculator.score(
                RecommendationContext.forHome(taste, Map.of(1L, 1.0), Map.of()), show, weights);

        assertThat(generator.homeReason(scored, weights, taste, Optional.of("폭풍의 언덕")))
                .isEqualTo("'폭풍의 언덕'을 좋아한 당신에게");
        assertThat(generator.homeReason(scored, weights, taste, Optional.of("아주 아주 길고 긴 인생작 제목입니다")))
                .isEqualTo("인생작과 닮은 작품");
    }

    @Test
    void 분위기_이유가_24자를_넘으면_짧은_문구를_쓴다() {
        ReasonGenerator generator = new ReasonGenerator(properties);
        Map<TasteTag, Double> moodTaste = Map.of(TasteTag.TENSE, 1.0);
        ShowQuery.Candidate show = candidate(1L, Map.of(TasteTag.TENSE, 0.9));
        ScoredShow scored = calculator.score(
                RecommendationContext.forHome(moodTaste, Map.of(), Map.of()), show, weights);

        String reason = generator.homeReason(scored, weights, moodTaste, Optional.empty());

        assertThat(reason).isEqualTo("긴장감 있고 몰입감 있는 작품");
        assertThat(reason.length()).isLessThanOrEqualTo(24);
    }

    private ScoredShow scored(Long id, TasteTag dominantGenre, double total) {
        return new ScoredShow(candidate(id, Map.of(dominantGenre, 0.9)), total, Map.of());
    }

    private ShowQuery.Candidate candidate(Long id, Map<TasteTag, Double> tagScores) {
        return new ShowQuery.Candidate(id, "공연" + id, "PLAY", null,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), tagScores);
    }
}
