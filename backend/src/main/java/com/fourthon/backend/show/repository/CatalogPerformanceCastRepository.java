package com.fourthon.backend.show.repository;

import com.fourthon.backend.show.domain.CatalogPerformanceCast;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CatalogPerformanceCastRepository extends JpaRepository<CatalogPerformanceCast, Long> {

    @Query("""
            SELECT c FROM CatalogPerformanceCast c
            JOIN FETCH c.actor
            WHERE c.performance.id = :performanceId
            ORDER BY c.castOrder
            """)
    List<CatalogPerformanceCast> findCastWithActors(@Param("performanceId") Long performanceId);
}
