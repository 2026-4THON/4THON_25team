package com.fourthon.backend.show.repository;

import com.fourthon.backend.show.domain.ShowDetail;
import com.fourthon.backend.ticket.domain.CatalogPerformance;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ShowCandidateRepository extends Repository<ShowDetail, Long> {

    @Query("""
            SELECT p.id AS performanceId, p.title AS title, p.genre AS genre, p.posterUrl AS posterUrl,
                   d.startDate AS startDate, d.endDate AS endDate
            FROM ShowDetail d, CatalogPerformance p
            WHERE p.id = d.performanceId
              AND d.analyzedAt IS NOT NULL
              AND (:genre IS NULL OR p.genre = :genre)
              AND d.endDate >= :today
              AND (:ongoingOnly = false OR d.startDate <= :today)
              AND d.performanceId NOT IN (
                  SELECT t.performance.id FROM Ticket t WHERE t.user.id = :userId
              )
            """)
    List<ShowRow> findCandidates(
            @Param("genre") CatalogPerformance.Genre genre,
            @Param("ongoingOnly") boolean ongoingOnly,
            @Param("today") LocalDate today,
            @Param("userId") Long userId
    );

    @Query("""
            SELECT p.id AS performanceId, p.title AS title, p.genre AS genre, p.posterUrl AS posterUrl,
                   d.startDate AS startDate, d.endDate AS endDate, d.minPrice AS minPrice,
                   COALESCE(d.synopsis, d.extractedSynopsis) AS synopsis
            FROM ShowDetail d, CatalogPerformance p
            WHERE p.id = d.performanceId
              AND d.startDate <= :today
              AND d.endDate >= :today
              AND d.minPrice IS NOT NULL
              AND d.minPrice <= :budget
            """)
    List<ExploreRow> findExploreCandidates(@Param("today") LocalDate today, @Param("budget") int budget);

    @Query("""
            SELECT s.performance.id AS performanceId, s.startsAt AS startsAt
            FROM CatalogSession s
            WHERE s.performance.id IN :performanceIds
              AND s.startsAt >= :from
              AND s.startsAt < :to
            """)
    List<SessionRow> findSessions(
            @Param("performanceIds") Collection<Long> performanceIds,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("SELECT MAX(r.rankedDate) FROM BoxOfficeRank r")
    LocalDate findLatestRankedDate();

    @Query("""
            SELECT r.rankNo AS rankNo, p.id AS performanceId, p.title AS title, p.genre AS genre,
                   p.posterUrl AS posterUrl, d.startDate AS startDate, d.endDate AS endDate
            FROM BoxOfficeRank r, CatalogPerformance p, ShowDetail d
            WHERE r.rankedDate = :rankedDate
              AND p.id = r.performanceId
              AND d.performanceId = r.performanceId
              AND (:genre IS NULL OR p.genre = :genre)
            ORDER BY r.rankNo
            """)
    List<RankedRow> findRanked(
            @Param("rankedDate") LocalDate rankedDate,
            @Param("genre") CatalogPerformance.Genre genre,
            Pageable pageable
    );

    interface ShowRow {

        Long getPerformanceId();

        String getTitle();

        CatalogPerformance.Genre getGenre();

        String getPosterUrl();

        LocalDate getStartDate();

        LocalDate getEndDate();
    }

    interface ExploreRow extends ShowRow {

        Integer getMinPrice();

        String getSynopsis();
    }

    interface SessionRow {

        Long getPerformanceId();

        LocalDateTime getStartsAt();
    }

    interface RankedRow extends ShowRow {

        Integer getRankNo();
    }
}
