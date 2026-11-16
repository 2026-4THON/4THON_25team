package com.fourthon.backend.recommendation.config;

import com.fourthon.backend.recommendation.score.FactorType;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.EnumMap;
import java.util.Map;

@ConfigurationProperties(prefix = "recommendation")
public record RecommendationProperties(
        @DefaultValue Home home,
        @DefaultValue("9") int popularSize,
        @DefaultValue("24") int reasonMaxLength,
        @DefaultValue Reason reason,
        @DefaultValue Explore explore
) {

    public record Explore(
            @DefaultValue("4") int size,
            @DefaultValue("50") int maxLlmCandidates,
            @DefaultValue("0.3") double minMatch,
            @DefaultValue("10") int popularityRankCount,
            @DefaultValue("300") int synopsisMaxLength,
            @DefaultValue ExploreWeights weights
    ) {
    }

    public record ExploreWeights(
            @DefaultValue("0.35") double genre,
            @DefaultValue("0.25") double mood,
            @DefaultValue("0.25") double wish,
            @DefaultValue("0.10") double rating,
            @DefaultValue("0.05") double popularity
    ) {

        public Map<FactorType, Double> toMap(boolean includeWish) {
            Map<FactorType, Double> weights = new EnumMap<>(FactorType.class);
            weights.put(FactorType.GENRE, genre);
            weights.put(FactorType.MOOD, mood);
            if (includeWish) {
                weights.put(FactorType.WISH, wish);
            }
            weights.put(FactorType.RATING, rating);
            weights.put(FactorType.POPULARITY, popularity);
            return weights;
        }
    }

    public record Reason(
            @DefaultValue("3") int evidenceCount,
            @DefaultValue("300") int evidenceMaxLength,
            @DefaultValue("40") int maxTargets,
            @DefaultValue("4000") long requestIntervalMillis
    ) {
    }

    public record Home(
            @DefaultValue("5") int size,
            @DefaultValue("3") int accurateCount,
            @DefaultValue("0.3") double minMatch,
            @DefaultValue HomeWeights weights
    ) {
    }

    public record HomeWeights(
            @DefaultValue("0.45") double genre,
            @DefaultValue("0.30") double mood,
            @DefaultValue("0.15") double precedent,
            @DefaultValue("0.10") double rating
    ) {

        public Map<FactorType, Double> toMap() {
            Map<FactorType, Double> weights = new EnumMap<>(FactorType.class);
            weights.put(FactorType.GENRE, genre);
            weights.put(FactorType.MOOD, mood);
            weights.put(FactorType.PRECEDENT, precedent);
            weights.put(FactorType.RATING, rating);
            return weights;
        }
    }
}
