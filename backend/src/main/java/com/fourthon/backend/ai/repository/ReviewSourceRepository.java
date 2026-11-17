package com.fourthon.backend.ai.repository;

import com.fourthon.backend.ticket.domain.TicketReview;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ReviewSourceRepository extends Repository<TicketReview, Long> {

    @Query("""
            SELECT r FROM TicketReview r
            JOIN FETCH r.ticket t
            JOIN FETCH t.performance
            WHERE t.id = :ticketId
            """)
    List<TicketReview> findByTicketId(@Param("ticketId") Long ticketId);

    @Query("""
            SELECT r FROM TicketReview r
            JOIN FETCH r.ticket t
            JOIN FETCH t.performance
            WHERE r.id IN :reviewIds
            """)
    List<TicketReview> findByIds(@Param("reviewIds") Collection<Long> reviewIds);

    @Query("""
            SELECT r.id FROM TicketReview r
            WHERE (:performanceId IS NULL OR r.ticket.performance.id = :performanceId)
              AND NOT EXISTS (
                  SELECT e.id FROM ReviewEmbedding e
                  WHERE e.reviewId = r.id AND e.model = :model
              )
              AND NOT EXISTS (
                  SELECT f.reviewId FROM ReviewAnalysisFailure f
                  WHERE f.reviewId = r.id AND f.failCount >= :maxFailCount
              )
            ORDER BY r.id
            """)
    List<Long> findMissingIds(
            @Param("performanceId") Long performanceId,
            @Param("model") String model,
            @Param("maxFailCount") int maxFailCount,
            Pageable pageable
    );
}
