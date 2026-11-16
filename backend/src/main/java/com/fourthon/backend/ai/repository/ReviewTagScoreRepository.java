package com.fourthon.backend.ai.repository;

import com.fourthon.backend.ai.domain.ReviewTagScore;
import com.fourthon.backend.common.domain.TasteTag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewTagScoreRepository extends JpaRepository<ReviewTagScore, Long> {

    @Query("""
            SELECT s.tag AS tag, AVG(s.score) AS average
            FROM ReviewTagScore s
            WHERE s.performanceId = :performanceId
            GROUP BY s.tag
            """)
    List<TagAverage> findAverages(@Param("performanceId") Long performanceId);

    @Query("SELECT COUNT(DISTINCT s.reviewId) FROM ReviewTagScore s WHERE s.performanceId = :performanceId")
    long countReviews(@Param("performanceId") Long performanceId);

    @Query("SELECT DISTINCT s.performanceId FROM ReviewTagScore s WHERE s.ticketId = :ticketId")
    List<Long> findPerformanceIdsByTicketId(@Param("ticketId") Long ticketId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM ReviewTagScore s WHERE s.ticketId = :ticketId")
    void deleteByTicketId(@Param("ticketId") Long ticketId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM ReviewTagScore s WHERE s.reviewId = :reviewId")
    void deleteByReviewId(@Param("reviewId") Long reviewId);

    interface TagAverage {

        TasteTag getTag();

        Double getAverage();
    }
}
