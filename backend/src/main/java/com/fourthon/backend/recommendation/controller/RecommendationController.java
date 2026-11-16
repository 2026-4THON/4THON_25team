package com.fourthon.backend.recommendation.controller;

import com.fourthon.backend.auth.resolver.CurrentUser;
import com.fourthon.backend.recommendation.domain.GenreTab;
import com.fourthon.backend.recommendation.domain.HomeScope;
import com.fourthon.backend.recommendation.dto.RecommendationResponse;
import com.fourthon.backend.recommendation.service.HomeRecommendationService;
import com.fourthon.backend.recommendation.service.PopularService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(
        name = "추천 API",
        description = "홈 취향 추천과 인기 공연"
)
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final HomeRecommendationService homeRecommendationService;
    private final PopularService popularService;

    @Operation(
            summary = "홈 취향 추천",
            description = "genre: ALL, MUSICAL, PLAY, ETC / scope: ONGOING(진행중), ALL(예정+진행중+완료). 리포트가 없으면 hasReport=false"
    )
    @GetMapping("/home")
    public RecommendationResponse.Home home(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @RequestParam(defaultValue = "ALL") GenreTab genre,
            @RequestParam(defaultValue = "ONGOING") HomeScope scope) {

        return homeRecommendationService.recommend(userId, genre, scope);
    }

    @Operation(summary = "지금 가장 인기 있는 공연", description = "최신 예매 순위에서 장르 탭별 상위 9개")
    @GetMapping("/popular")
    public List<RecommendationResponse.PopularItem> popular(
            @RequestParam(defaultValue = "ALL") GenreTab genre) {

        return popularService.findPopular(genre);
    }
}
