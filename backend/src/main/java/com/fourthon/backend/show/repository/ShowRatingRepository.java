package com.fourthon.backend.show.repository;

import com.fourthon.backend.ticket.domain.Ticket;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

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

    interface RatingSummary {

        Long getRatingCount();

        Double getAverageHalfUnits();
    }
}
