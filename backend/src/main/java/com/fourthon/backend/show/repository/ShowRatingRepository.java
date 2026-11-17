package com.fourthon.backend.show.repository;

import com.fourthon.backend.ticket.domain.Ticket;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ShowRatingRepository extends Repository<Ticket, Long> {

    @Query("""
            SELECT COUNT(t) AS ratingCount, AVG(t.ratingHalfUnits) AS averageHalfUnits
            FROM Ticket t
            WHERE t.performance.id = :performanceId
            """)
    RatingSummary findSummary(@Param("performanceId") Long performanceId);

    @Query("""
            SELECT COUNT(t)
            FROM Ticket t
            GROUP BY t.performance.id
            ORDER BY COUNT(t)
            """)
    List<Long> findRatingCountsPerShow();

    @Query("SELECT AVG(t.ratingHalfUnits) FROM Ticket t")
    Double findAverageHalfUnits();

    @Query("""
            SELECT t.performance.id AS performanceId, COUNT(t) AS ratingCount, AVG(t.ratingHalfUnits) AS averageHalfUnits
            FROM Ticket t
            WHERE t.performance.id IN :performanceIds
            GROUP BY t.performance.id
            """)
    List<PerformanceRating> findSummaries(@Param("performanceIds") Collection<Long> performanceIds);

    @Query("""
            SELECT t.performance.id AS performanceId, COUNT(r) AS reviewCount
            FROM TicketReview r JOIN r.ticket t
            WHERE t.performance.id IN :performanceIds
            GROUP BY t.performance.id
            """)
    List<ReviewCount> findReviewCounts(@Param("performanceIds") Collection<Long> performanceIds);

    interface ReviewCount {

        Long getPerformanceId();

        Long getReviewCount();
    }

    interface PerformanceRating {

        Long getPerformanceId();

        Long getRatingCount();

        Double getAverageHalfUnits();
    }

    interface RatingSummary {

        Long getRatingCount();

        Double getAverageHalfUnits();
    }
}
