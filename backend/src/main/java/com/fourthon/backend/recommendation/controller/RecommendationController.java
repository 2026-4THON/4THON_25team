package com.fourthon.backend.recommendation.controller;

import com.fourthon.backend.auth.resolver.CurrentUser;
import com.fourthon.backend.recommendation.domain.GenreTab;
import com.fourthon.backend.recommendation.domain.HomeScope;
import com.fourthon.backend.recommendation.dto.RecommendationRequest;
import com.fourthon.backend.recommendation.dto.RecommendationResponse;
import com.fourthon.backend.recommendation.service.ExploreService;
import com.fourthon.backend.recommendation.service.HomeRecommendationService;
import com.fourthon.backend.recommendation.service.PairRecommendationService;
import com.fourthon.backend.recommendation.service.PopularService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(
        name = "추천 API",
        description = "홈 취향 추천, 인기 공연, 탐색 추천, AI 페어 추천"
)
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final HomeRecommendationService homeRecommendationService;
    private final PopularService popularService;
    private final ExploreService exploreService;
    private final PairRecommendationService pairRecommendationService;

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

    @Operation(
            summary = "탐색 추천",
            description = "진행중·희망 날짜 회차·예산 조건으로 거른 뒤 최대 4개 추천. 조건에 맞는 공연이 없으면 status=NO_MATCH, 리포트가 없으면 404 TASTE_REPORT_NOT_FOUND"
    )
    @PostMapping("/explore")
    public RecommendationResponse.Explore explore(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @Valid @RequestBody RecommendationRequest.Explore request) {

        return exploreService.explore(userId, request);
    }

    @Operation(
            summary = "AI 페어 추천",
            description = "내 취향과 이 작품에 쓴 내 후기를 기준으로 다른 사람의 페어 후기에서 배우 조합을 추천. 처음 요청 때 만들고 저장해 재사용"
    )
    @GetMapping("/pair/{performanceId}")
    public RecommendationResponse.Pair pair(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @PathVariable Long performanceId) {

        return pairRecommendationService.recommend(userId, performanceId);
    }
}
