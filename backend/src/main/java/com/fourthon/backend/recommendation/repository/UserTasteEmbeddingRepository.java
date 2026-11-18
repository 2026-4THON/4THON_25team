package com.fourthon.backend.recommendation.repository;

import com.fourthon.backend.recommendation.domain.UserTasteEmbedding;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserTasteEmbeddingRepository extends JpaRepository<UserTasteEmbedding, Long> {

    Optional<UserTasteEmbedding> findByUserIdAndPerformanceIdAndModel(Long userId, Long performanceId, String model);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM UserTasteEmbedding e WHERE e.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM UserTasteEmbedding e WHERE e.userId = :userId AND e.performanceId = :performanceId")
    void deleteByUserIdAndPerformanceId(@Param("userId") Long userId, @Param("performanceId") Long performanceId);
}
