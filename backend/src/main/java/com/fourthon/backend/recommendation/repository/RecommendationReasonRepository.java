package com.fourthon.backend.recommendation.repository;

import com.fourthon.backend.recommendation.domain.RecommendationReason;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface RecommendationReasonRepository extends JpaRepository<RecommendationReason, Long> {

    List<RecommendationReason> findByUserIdAndPerformanceIdIn(Long userId, Collection<Long> performanceIds);

    @Query("SELECT r.performanceId FROM RecommendationReason r WHERE r.userId = :userId")
    List<Long> findPerformanceIdsByUserId(@Param("userId") Long userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM RecommendationReason r WHERE r.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
