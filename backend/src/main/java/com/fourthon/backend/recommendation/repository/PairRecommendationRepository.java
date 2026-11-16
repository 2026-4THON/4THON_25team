package com.fourthon.backend.recommendation.repository;

import com.fourthon.backend.recommendation.domain.PairRecommendation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PairRecommendationRepository extends JpaRepository<PairRecommendation, Long> {

    Optional<PairRecommendation> findByUserIdAndPerformanceId(Long userId, Long performanceId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM PairRecommendation p WHERE p.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
