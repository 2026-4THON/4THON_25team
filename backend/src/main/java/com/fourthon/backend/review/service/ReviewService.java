
package com.fourthon.backend.review.service;

import com.fourthon.backend.review.dto.ReviewResponse;
import com.fourthon.backend.review.repository.ReviewLikeRepository;
import com.fourthon.backend.review.repository.ReviewRepository;
import com.fourthon.backend.ticket.domain.TicketReview;
import com.fourthon.backend.ticket.domain.TicketReviewActor;
import com.fourthon.backend.ticket.domain.TicketReviewLike;
import com.fourthon.backend.user.entity.User;
import com.fourthon.backend.user.repository.UserRepository;

import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReviewService {

    private final ReviewRepository reviews;
    private final ReviewLikeRepository likes;
    private final UserRepository users;

    public ReviewService(
            ReviewRepository reviews,
            ReviewLikeRepository likes,
            UserRepository users
    ) {
        this.reviews = reviews;
        this.likes = likes;
        this.users = users;
    }

    private ResponseStatusException bad(String message) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST, message
        );
    }

    private TicketReview requireReview(Long reviewId) {
        return reviews.findById(reviewId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "후기를 찾을 수 없습니다"
                        )
                );
    }

    private User requireUser(Long userId) {
        return users.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "로그인 회원을 찾을 수 없습니다"
                        )
                );
    }

    private void validatePage(
            String sort,
            int page,
            int size
    ) {
        if (!sort.equals("latest") && !sort.equals("likes")) {
            throw bad("정렬은 latest 또는 likes만 가능합니다");
        }

        if (page < 0) {
            throw bad("page는 0 이상이어야 합니다");
        }

        if (size < 1 || size > 100) {
            throw bad("size는 1부터 100까지 가능합니다");
        }
    }

    @Transactional(readOnly = true)
    public ReviewResponse.PageResult list(
            Long userId,
            Long performanceId,
            TicketReview.ReviewType type,
            String actorName,
            List<Long> actorIds,
            String sort,
            boolean mineOnly,
            int page,
            int size
    ) {
        validatePage(sort, page, size);

        if (performanceId == null || performanceId <= 0) {
            throw bad("올바른 공연 ID를 입력해 주세요");
        }

        if (type == TicketReview.ReviewType.ACTOR) {
            if (actorName == null || actorName.isBlank()) {
                throw bad("배우 이름을 입력해 주세요");
            }
            actorName = actorName.trim();
        } else {
            actorName = null;
        }

        if (type == TicketReview.ReviewType.PAIR) {
            if (actorIds == null || actorIds.size() < 2) {
                throw bad("페어는 배우 2명 이상을 선택해야 합니다");
            }

            if (actorIds.stream().anyMatch(
                    id -> id == null || id <= 0
            )) {
                throw bad("올바른 배우 ID를 입력해 주세요");
            }

            if (new HashSet<>(actorIds).size()
                    != actorIds.size()) {
                throw bad("동일 배우를 중복 선택할 수 없습니다");
            }
        }

        final String selectedActorName = actorName;
        final List<Long> selectedPair =
                actorIds == null ? List.of() : List.copyOf(actorIds);

        Specification<TicketReview> specification =
                (root, query, cb) -> {

                    List<Predicate> conditions =
                            new ArrayList<>();

                    Path<?> ticket = root.get("ticket");

                    // 작품 필터
                    conditions.add(
                            cb.equal(
                                    ticket.get("performance").get("id"),
                                    performanceId
                            )
                    );

                    // 공연 / 배우 / 페어 후기 유형
                    conditions.add(
                            cb.equal(root.get("type"), type)
                    );

                    // 내 후기만 보기
                    if (mineOnly) {
                        conditions.add(
                                cb.equal(
                                        ticket.get("user").get("id"),
                                        userId
                                )
                        );
                    }

                    // 배우 이름으로 필터
                    if (type == TicketReview.ReviewType.ACTOR) {

                        Subquery<Long> actorQuery =
                                query.subquery(Long.class);

                        Root<TicketReviewActor> actorRoot =
                                actorQuery.from(TicketReviewActor.class);

                        actorQuery.select(
                                actorRoot.get("id")
                        );

                        actorQuery.where(
                                cb.equal(
                                        actorRoot.get("review"),
                                        root
                                ),
                                cb.equal(
                                        actorRoot.get("actor").get("name"),
                                        selectedActorName
                                )
                        );

                        conditions.add(
                                cb.exists(actorQuery)
                        );
                    }

                    // 페어: 배우 조합이 정확히 일치해야 함
                    // 순서는 비교하지 않음
                    if (type == TicketReview.ReviewType.PAIR) {

                        Subquery<Long> pairQuery =
                                query.subquery(Long.class);

                        Root<TicketReviewActor> pairActor =
                                pairQuery.from(TicketReviewActor.class);

                        pairQuery.select(
                                cb.count(pairActor)
                        );

                        pairQuery.where(
                                cb.equal(
                                        pairActor.get("review"),
                                        root
                                ),
                                pairActor.get("actor")
                                        .get("id")
                                        .in(selectedPair)
                        );

                        // 전체 배우 수가 같고,
                        // 그 배우들이 모두 선택한 조합에 포함
                        conditions.add(
                                cb.equal(
                                        cb.size(root.get("actors")),
                                        selectedPair.size()
                                )
                        );

                        conditions.add(
                                cb.equal(
                                        pairQuery,
                                        (long) selectedPair.size()
                                )
                        );
                    }

                    // 페이지 조회에만 정렬 적용
                    // Count 쿼리에는 ORDER BY 적용하지 않음
                    if (query != null
                            && query.getResultType() != Long.class
                            && query.getResultType() != long.class) {

                        if ("likes".equals(sort)) {

                            Subquery<Long> likeCount =
                                    query.subquery(Long.class);

                            Root<TicketReviewLike> likeRoot =
                                    likeCount.from(
                                            TicketReviewLike.class
                                    );

                            likeCount.select(
                                    cb.count(likeRoot)
                            );

                            likeCount.where(
                                    cb.equal(
                                            likeRoot.get("review"),
                                            root
                                    )
                            );

                            query.orderBy(
                                    cb.desc(likeCount),
                                    cb.desc(root.get("id"))
                            );

                        } else {

                            Expression<LocalDateTime> createdAt =
                                    cb.coalesce(
                                            root.get("createdAt"),
                                            ticket.get("createdAt")
                                    );

                            query.orderBy(
                                    cb.desc(createdAt),
                                    cb.desc(root.get("id"))
                            );
                        }
                    }

                    return cb.and(
                            conditions.toArray(new Predicate[0])
                    );
                };

        Page<TicketReview> result = reviews.findAll(
                specification,
                PageRequest.of(page, size)
        );

        List<ReviewResponse.Item> items =
                result.getContent()
                        .stream()
                        .map(review -> toItem(review, userId))
                        .toList();

        return new ReviewResponse.PageResult(
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }

    private ReviewResponse.Item toItem(
            TicketReview review,
            Long userId
    ) {
        Long reviewId = review.getId();

        List<ReviewResponse.Actor> actors =
                review.getActors()
                        .stream()
                        .map(a -> new ReviewResponse.Actor(
                                a.getActor().getId(),
                                a.getActor().getName()
                        ))
                        .toList();

        long likeCount =
                likes.countByReview_Id(reviewId);

        boolean likedByMe =
                likes.existsByReview_IdAndUser_Id(
                        reviewId,
                        userId
                );

        LocalDateTime createdAt = review.getCreatedAt();

        if (createdAt == null) {
            createdAt = review.getTicket().getCreatedAt();
        }

        return new ReviewResponse.Item(
                reviewId,
                review.getTicket().getId(),
                review.getType().name(),
                review.getTicket().getUser().getId(),
                review.getTicket().getUser().getName(),
                review.getContent(),
                review.getTicket()
                        .getViewedAt()
                        .toLocalDate(),
                createdAt,
                actors,
                likeCount,
                likedByMe
        );
    }

    // 후기 좋아요 등록
    @Transactional
    public ReviewResponse.LikeResult addLike(
            Long userId,
            Long reviewId
    ) {
        TicketReview review = requireReview(reviewId);

        boolean alreadyLiked =
                likes.existsByReview_IdAndUser_Id(
                        reviewId,
                        userId
                );

        if (!alreadyLiked) {
            User user = requireUser(userId);

            TicketReviewLike like =
                    new TicketReviewLike(review, user);

            likes.saveAndFlush(like);
        }

        return likeResult(userId, reviewId);
    }

    // 후기 좋아요 취소
    @Transactional
    public ReviewResponse.LikeResult removeLike(
            Long userId,
            Long reviewId
    ) {
        requireReview(reviewId);

        likes.findByReview_IdAndUser_Id(
                reviewId,
                userId
        ).ifPresent(like -> {
            likes.delete(like);
            likes.flush();
        });

        return likeResult(userId, reviewId);
    }

    private ReviewResponse.LikeResult likeResult(
            Long userId,
            Long reviewId
    ) {
        return new ReviewResponse.LikeResult(
                reviewId,
                likes.countByReview_Id(reviewId),
                likes.existsByReview_IdAndUser_Id(
                        reviewId,
                        userId
                )
        );
    }
}
