package com.fourthon.backend.ai.repository;

import com.fourthon.backend.ai.domain.ReviewEmbedding;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewEmbeddingRepository extends JpaRepository<ReviewEmbedding, Long> {

    List<ReviewEmbedding> findByPerformanceIdAndModel(Long performanceId, String model);

    @Query("SELECT DISTINCT e.performanceId FROM ReviewEmbedding e WHERE e.ticketId = :ticketId")
    List<Long> findPerformanceIdsByTicketId(@Param("ticketId") Long ticketId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM ReviewEmbedding e WHERE e.ticketId = :ticketId")
    void deleteByTicketId(@Param("ticketId") Long ticketId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM ReviewEmbedding e WHERE e.reviewId = :reviewId")
    void deleteByReviewId(@Param("reviewId") Long reviewId);
}
