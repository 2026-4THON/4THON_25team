
package com.fourthon.backend.review.controller;

import com.fourthon.backend.auth.resolver.CurrentUser;
import com.fourthon.backend.review.dto.ReviewResponse;
import com.fourthon.backend.review.service.ReviewService;
import com.fourthon.backend.ticket.domain.TicketReview;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(
        name = "후기 API",
        description = "작품·배우·페어 후기 조회 및 좋아요"
)
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(
            summary = "작품 후기 목록 조회",
            description = "최신순·좋아요순 정렬, 내 후기 필터, 페이지네이션"
    )
    @GetMapping("/api/shows/{performanceId}/reviews")
    public ReviewResponse.PageResult performanceReviews(
            @Parameter(hidden = true)
            @CurrentUser Long userId,

            @PathVariable Long performanceId,

            @RequestParam(defaultValue = "latest")
            String sort,

            @RequestParam(defaultValue = "false")
            boolean mineOnly,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {
        return reviewService.list(
                userId,
                performanceId,
                TicketReview.ReviewType.PERFORMANCE,
                null,
                null,
                sort,
                mineOnly,
                page,
                size
        );
    }

    @Operation(
            summary = "배우별 후기 목록 조회",
            description = "작품 ID와 배우 이름으로 조회"
    )
    @GetMapping("/api/shows/{performanceId}/actors/reviews")
    public ReviewResponse.PageResult actorReviews(
            @Parameter(hidden = true)
            @CurrentUser Long userId,

            @PathVariable Long performanceId,

            @RequestParam String actorName,

            @RequestParam(defaultValue = "latest")
            String sort,

            @RequestParam(defaultValue = "false")
            boolean mineOnly,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {
        return reviewService.list(
                userId,
                performanceId,
                TicketReview.ReviewType.ACTOR,
                actorName,
                null,
                sort,
                mineOnly,
                page,
                size
        );
    }

    @Operation(
            summary = "페어 후기 목록 조회",
            description = "동일한 배우 조합을 순서와 관계없이 조회"
    )
    @GetMapping("/api/shows/{performanceId}/pairs/reviews")
    public ReviewResponse.PageResult pairReviews(
            @Parameter(hidden = true)
            @CurrentUser Long userId,

            @PathVariable Long performanceId,

            @RequestParam List<Long> actorIds,

            @RequestParam(defaultValue = "latest")
            String sort,

            @RequestParam(defaultValue = "false")
            boolean mineOnly,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {
        return reviewService.list(
                userId,
                performanceId,
                TicketReview.ReviewType.PAIR,
                null,
                actorIds,
                sort,
                mineOnly,
                page,
                size
        );
    }

    @Operation(
            summary = "후기 좋아요 등록",
            description = "한 사용자당 동일 후기 1회만 등록"
    )
    @PostMapping("/api/reviews/{reviewId}/likes")
    public ReviewResponse.LikeResult addLike(
            @Parameter(hidden = true)
            @CurrentUser Long userId,

            @PathVariable Long reviewId
    ) {
        return reviewService.addLike(
                userId, reviewId
        );
    }

    @Operation(
            summary = "후기 좋아요 취소",
            description = "현재 로그인한 사용자의 좋아요 취소"
    )
    @DeleteMapping("/api/reviews/{reviewId}/likes")
    public ReviewResponse.LikeResult removeLike(
            @Parameter(hidden = true)
            @CurrentUser Long userId,

            @PathVariable Long reviewId
    ) {
        return reviewService.removeLike(
                userId, reviewId
        );
    }
}
