package com.fourthon.backend.recommendation.score;

import com.fourthon.backend.common.domain.TagScores;
import com.fourthon.backend.common.domain.TagType;
import com.fourthon.backend.common.domain.TasteTag;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class TasteMatch {

    private TasteMatch() {
    }

    public static double match(Map<TasteTag, Double> tasteScores, Map<TasteTag, Double> showScores, TagType type) {
        double weightSum = 0.0;
        double weighted = 0.0;
        for (TasteTag tag : TasteTag.values()) {
            if (tag.getType() != type) {
                continue;
            }
            double preference = tasteScores.getOrDefault(tag, 0.0);
            weightSum += preference;
            weighted += preference * showScores.getOrDefault(tag, 0.0);
        }
        return weightSum == 0.0 ? 0.0 : TagScores.clamp(weighted / weightSum);
    }

    public static Optional<TasteTag> bestTag(
            Map<TasteTag, Double> tasteScores,
            Map<TasteTag, Double> showScores,
            TagType type) {

        return tagsOf(type)
                .filter(tag -> tasteScores.getOrDefault(tag, 0.0) * showScores.getOrDefault(tag, 0.0) > 0.0)
                .max(Comparator.comparingDouble(tag ->
                        tasteScores.getOrDefault(tag, 0.0) * showScores.getOrDefault(tag, 0.0)));
    }

    public static Optional<TasteTag> dominantTag(Map<TasteTag, Double> showScores, TagType type) {
        return tagsOf(type)
                .filter(tag -> showScores.getOrDefault(tag, 0.0) > 0.0)
                .max(Comparator.comparingDouble(tag -> showScores.getOrDefault(tag, 0.0)));
    }

    private static Stream<TasteTag> tagsOf(TagType type) {
        return Stream.of(TasteTag.values()).filter(tag -> tag.getType() == type);
    }
}
