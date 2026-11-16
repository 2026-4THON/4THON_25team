package com.fourthon.backend.taste.repository;

import com.fourthon.backend.taste.domain.TasteReport;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TasteReportRepository extends JpaRepository<TasteReport, Long> {

    Optional<TasteReport> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    @Query("""
            SELECT DISTINCT r.userId
            FROM TasteReport r JOIN r.lifeWorks w
            WHERE w.normalizedTitle IN :titles
              AND r.userId <> :userId
            """)
    List<Long> findPeerUserIds(@Param("titles") Collection<String> titles, @Param("userId") Long userId);
}
