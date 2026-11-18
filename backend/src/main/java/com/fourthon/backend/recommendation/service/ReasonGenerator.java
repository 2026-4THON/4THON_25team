package com.fourthon.backend.recommendation.service;

import com.fourthon.backend.common.domain.TagType;
import com.fourthon.backend.common.domain.TasteTag;
import com.fourthon.backend.recommendation.config.RecommendationProperties;
import com.fourthon.backend.recommendation.score.FactorType;
import com.fourthon.backend.recommendation.score.ScoredShow;
import com.fourthon.backend.recommendation.score.TasteMatch;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReasonGenerator {

    private static final String DEFAULT_REASON = "당신의 취향과 닮은 작품";
    private static final String LIFE_WORK_FALLBACK = "인생작과 닮은 작품";
    private static final char HANGUL_START = '가';
    private static final char HANGUL_END = '힣';
    private static final int JONGSEONG_COUNT = 28;

    private final RecommendationProperties recommendationProperties;

    public String homeReason(
            ScoredShow show,
            Map<FactorType, Double> weights,
            Map<TasteTag, Double> tasteScores,
            Optional<String> lifeWorkTitle) {

        FactorType topFactor = topFactor(show, weights);
        if (topFactor == FactorType.PRECEDENT && lifeWorkTitle.isPresent()) {
            return lifeWorkReason(lifeWorkTitle.get());
        }
        TagType tagType = topFactor == FactorType.MOOD ? TagType.MOOD : TagType.GENRE;
        Optional<TasteTag> tag = TasteMatch.bestTag(tasteScores, show.candidate().tagScores(), tagType)
                .or(() -> TasteMatch.bestTag(tasteScores, show.candidate().tagScores(),
                        tagType == TagType.GENRE ? TagType.MOOD : TagType.GENRE));
        return tag.map(this::tagReason).orElse(DEFAULT_REASON);
    }

    public boolean fits(String reason) {
        return reason != null && !reason.isBlank() && reason.length() <= recommendationProperties.reasonMaxLength();
    }

    private FactorType topFactor(ScoredShow show, Map<FactorType, Double> weights) {
        FactorType best = FactorType.GENRE;
        double bestContribution = -1.0;
        for (FactorType type : List.of(FactorType.GENRE, FactorType.MOOD, FactorType.PRECEDENT)) {
            double contribution = weights.getOrDefault(type, 0.0) * show.factor(type);
            if (contribution > bestContribution) {
                best = type;
                bestContribution = contribution;
            }
        }
        return best;
    }

    private String lifeWorkReason(String title) {
        String reason = "'" + title + "'" + objectParticle(title) + " 좋아한 당신에게";
        return fits(reason) ? reason : LIFE_WORK_FALLBACK;
    }

    private String tagReason(TasteTag tag) {
        String subject = tag.getType() == TagType.MOOD ? tag.getLabel() + " 작품" : tag.getLabel();
        String reason = subject + objectParticle(subject) + " 좋아하는 당신에게";
        if (fits(reason)) {
            return reason;
        }
        String shortReason = tag.getLabel() + " 작품";
        return fits(shortReason) ? shortReason : DEFAULT_REASON;
    }

    private String objectParticle(String word) {
        for (int i = word.length() - 1; i >= 0; i--) {
            char last = word.charAt(i);
            if (last >= HANGUL_START && last <= HANGUL_END) {
                return (last - HANGUL_START) % JONGSEONG_COUNT == 0 ? "를" : "을";
            }
            if (Character.isLetterOrDigit(last)) {
                return "를";
            }
        }
        return "를";
    }
}
