package com.fourthon.backend.recommendation.repository;

import com.fourthon.backend.ticket.domain.TicketReview;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PairReviewRepository extends Repository<TicketReview, Long> {

    @Query("""
            SELECT DISTINCT r FROM TicketReview r
            JOIN FETCH r.ticket t
            LEFT JOIN FETCH r.actors ra
            LEFT JOIN FETCH ra.actor
            WHERE t.performance.id = :performanceId
              AND r.type = :type
              AND t.user.id <> :userId
            """)
    List<TicketReview> findOthersReviews(
            @Param("performanceId") Long performanceId,
            @Param("type") TicketReview.ReviewType type,
            @Param("userId") Long userId
    );

    @Query("""
            SELECT r.content FROM TicketReview r
            WHERE r.ticket.performance.id = :performanceId
              AND r.ticket.user.id = :userId
              AND r.content IS NOT NULL
            """)
    List<String> findMyReviewContents(@Param("performanceId") Long performanceId, @Param("userId") Long userId);
}
