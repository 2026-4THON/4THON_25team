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
            WHERE d.analyzedAt IS NULL
              AND d.analysisFailCount < :maxFailCount
            ORDER BY CASE WHEN d.endDate >= :today THEN 0 ELSE 1 END, d.startDate DESC
            """)
    List<Long> findAnalysisTargets(
            @Param("today") LocalDate today,
            @Param("maxFailCount") int maxFailCount,
            Pageable pageable
    );

    @Query(value = """
            SELECT d.performance_id
            FROM show_details d
            WHERE d.extracted_synopsis IS NULL
              AND d.end_date >= :today
              AND (d.synopsis IS NULL OR CHAR_LENGTH(TRIM(d.synopsis)) < :minSynopsisLength)
              AND EXISTS (SELECT 1 FROM show_intro_images i WHERE i.performance_id = d.performance_id)
            ORDER BY CASE WHEN d.start_date <= :today THEN 0 ELSE 1 END, d.start_date DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Long> findExtractionTargets(
            @Param("today") LocalDate today,
            @Param("minSynopsisLength") int minSynopsisLength,
            @Param("limit") int limit
    );

    @Query(
            value = "SELECT image_url FROM show_intro_images WHERE performance_id = :performanceId ORDER BY sort_order",
            nativeQuery = true
    )
    List<String> findIntroImageUrls(@Param("performanceId") Long performanceId);
}
