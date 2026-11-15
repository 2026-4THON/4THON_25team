
package com.fourthon.backend.review.repository;

import com.fourthon.backend.ticket.domain.TicketReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewLikeRepository
        extends JpaRepository<TicketReviewLike, Long> {

    boolean existsByReview_IdAndUser_Id(
            Long reviewId,
            Long userId
    );

    Optional<TicketReviewLike> findByReview_IdAndUser_Id(
            Long reviewId,
            Long userId
    );

    long countByReview_Id(Long reviewId);
}
