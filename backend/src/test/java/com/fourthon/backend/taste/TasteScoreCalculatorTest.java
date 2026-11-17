package com.fourthon.backend.taste;

import com.fourthon.backend.common.domain.TasteTag;
import com.fourthon.backend.taste.service.TasteScoreCalculator;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class TasteScoreCalculatorTest {

    private final TasteScoreCalculator calculator = new TasteScoreCalculator();

    @Test
    void 별점_기록이_없으면_선택_60_AI_40으로_섞는다() {
        Map<TasteTag, Double> scores = calculator.calculate(
                Set.of(TasteTag.ROMANCE),
                Map.of(TasteTag.ROMANCE, 0.5, TasteTag.TENSE, 1.0),
                Map.of()
        );

        assertThat(scores.get(TasteTag.ROMANCE)).isCloseTo(0.6 + 0.2, within(1e-9));
        assertThat(scores.get(TasteTag.TENSE)).isCloseTo(0.4, within(1e-9));
        assertThat(scores.get(TasteTag.COMEDY)).isZero();
    }

    @Test
    void 세_가지가_모두_있으면_50_30_20으로_섞는다() {
        Map<TasteTag, Double> scores = calculator.calculate(
                Set.of(TasteTag.ROMANCE),
                Map.of(TasteTag.ROMANCE, 1.0),
                Map.of(TasteTag.ROMANCE, 1.0)
        );

        assertThat(scores.get(TasteTag.ROMANCE)).isCloseTo(1.0, within(1e-9));
    }

    @Test
    void AI_점수가_없으면_선택만_쓴다() {
        Map<TasteTag, Double> scores = calculator.calculate(Set.of(TasteTag.SF), Map.of(), Map.of());

        assertThat(scores.get(TasteTag.SF)).isEqualTo(1.0);
        assertThat(scores).hasSize(TasteTag.values().length);
    }
}
