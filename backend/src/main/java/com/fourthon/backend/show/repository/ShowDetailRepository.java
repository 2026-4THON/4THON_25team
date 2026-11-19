package com.fourthon.backend.show.repository;

import com.fourthon.backend.show.domain.ShowDetail;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ShowDetailRepository extends JpaRepository<ShowDetail, Long> {

    Optional<ShowDetail> findByKopisId(String kopisId);

    List<ShowDetail> findByKopisIdIn(Collection<String> kopisIds);

    List<ShowDetail> findByEndDateGreaterThanEqual(LocalDate date);

    @Query("""
            SELECT d.performanceId
            FROM ShowDetail d
            WHERE d.videoSearchStatus IS NULL
              AND d.endDate >= :today
            ORDER BY CASE WHEN d.startDate <= :today THEN 0 ELSE 1 END, d.startDate
            """)
    List<Long> findVideoSearchTargets(@Param("today") LocalDate today, Pageable pageable);

    @Query("""
            SELECT d.performanceId
            FROM ShowDetail d
            WHERE d.analyzedAt IS NULL
              AND d.analysisFailCount < :maxFailCount
            ORDER BY CASE WHEN d.endDate >= :today THEN 0 ELSE 1 END, d.startDate DESC
            """)
    List<Long> findAnalysisTargets(
            @Param("today") LocalDate today,
            @Param("maxFailCount") int maxFailCount,
            Pageable pageable
    );
}
