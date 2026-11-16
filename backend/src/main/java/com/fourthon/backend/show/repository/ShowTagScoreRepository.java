package com.fourthon.backend.show.repository;

import com.fourthon.backend.show.domain.ShowTagScore;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShowTagScoreRepository extends JpaRepository<ShowTagScore, Long> {

    List<ShowTagScore> findByPerformanceId(Long performanceId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM ShowTagScore s WHERE s.performanceId = :performanceId")
    void deleteByPerformanceId(@Param("performanceId") Long performanceId);
}
