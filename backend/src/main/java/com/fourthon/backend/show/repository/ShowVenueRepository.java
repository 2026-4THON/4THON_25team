package com.fourthon.backend.show.repository;

import com.fourthon.backend.show.domain.ShowDetail;
import com.fourthon.backend.ticket.domain.CatalogPerformance;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ShowVenueRepository extends Repository<ShowDetail, Long> {

    @Query("""
            SELECT d.venueId AS venueId, COUNT(d) AS itemCount
            FROM ShowDetail d
            WHERE d.venueId IS NOT NULL
              AND d.startDate <= :today
              AND d.endDate >= :today
            GROUP BY d.venueId
            """)
    List<VenueCount> countOngoingByVenue(@Param("today") LocalDate today);

    @Query("""
            SELECT p.id AS performanceId, p.title AS title, p.genre AS genre, p.posterUrl AS posterUrl,
                   d.startDate AS startDate, d.endDate AS endDate
            FROM ShowDetail d, CatalogPerformance p
            WHERE p.id = d.performanceId
              AND d.venueId = :venueId
              AND d.startDate <= :today
              AND d.endDate >= :today
            ORDER BY d.endDate, p.title
            """)
    List<VenueShowRow> findOngoingAtVenue(@Param("venueId") Long venueId, @Param("today") LocalDate today);

    @Query("""
            SELECT d.venueId AS venueId, COUNT(t) AS itemCount
            FROM Ticket t, ShowDetail d
            WHERE d.performanceId = t.performance.id
              AND t.user.id = :userId
              AND d.venueId IS NOT NULL
            GROUP BY d.venueId
            """)
    List<VenueCount> countTicketsByVenue(@Param("userId") Long userId);

    @Query("""
            SELECT t.id AS ticketId, p.id AS performanceId, p.title AS title, t.viewedAt AS viewedAt
            FROM Ticket t JOIN t.performance p, ShowDetail d
            WHERE d.performanceId = p.id
              AND t.user.id = :userId
              AND d.venueId = :venueId
            ORDER BY t.viewedAt DESC
            """)
    List<VenueTicketRow> findTicketsAtVenue(@Param("userId") Long userId, @Param("venueId") Long venueId);

    interface VenueCount {

        Long getVenueId();

        Long getItemCount();
    }

    interface VenueShowRow {

        Long getPerformanceId();

        String getTitle();

        CatalogPerformance.Genre getGenre();

        String getPosterUrl();

        LocalDate getStartDate();

        LocalDate getEndDate();
    }

    interface VenueTicketRow {

        Long getTicketId();

        Long getPerformanceId();

        String getTitle();

        LocalDateTime getViewedAt();
    }
}
