package com.fourthon.backend.taste.repository;

import com.fourthon.backend.ticket.domain.Ticket;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TicketRatingRepository extends Repository<Ticket, Long> {

    @Query("""
            SELECT t.user.id AS userId, t.performance.id AS performanceId, t.ratingHalfUnits AS ratingHalfUnits
            FROM Ticket t
            WHERE t.user.id IN :userIds
            """)
    List<RatingRow> findByUserIds(@Param("userIds") Collection<Long> userIds);

    interface RatingRow {

        Long getUserId();

        Long getPerformanceId();

        Integer getRatingHalfUnits();
    }
}
