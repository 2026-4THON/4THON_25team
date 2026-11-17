package com.fourthon.backend.recommendation.service;

import com.fourthon.backend.recommendation.config.RecommendationProperties;
import com.fourthon.backend.recommendation.domain.GenreTab;
import com.fourthon.backend.recommendation.dto.RecommendationResponse;
import com.fourthon.backend.show.domain.ShowStatus;
import com.fourthon.backend.show.dto.ShowQuery;
import com.fourthon.backend.show.service.ShowRecommendationService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class PopularService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final ShowRecommendationService showRecommendationService;
    private final RecommendationProperties recommendationProperties;

    public List<RecommendationResponse.PopularItem> findPopular(GenreTab genreTab) {
        LocalDate today = LocalDate.now(SEOUL);
        List<ShowQuery.RankedShow> ranks = showRecommendationService.findLatestRanks(
                genreTab.toGenre(),
                recommendationProperties.popularSize()
        );
        return IntStream.range(0, ranks.size())
                .mapToObj(index -> {
                    ShowQuery.RankedShow show = ranks.get(index);
                    return new RecommendationResponse.PopularItem(
                            index + 1,
                            show.performanceId(),
                            show.title(),
                            show.genre(),
                            show.posterUrl(),
                            ShowStatus.of(show.startDate(), show.endDate(), today).name()
                    );
                })
                .toList();
    }
}
