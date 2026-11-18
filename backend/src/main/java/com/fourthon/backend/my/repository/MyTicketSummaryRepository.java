
package com.fourthon.backend.my.repository;

import com.fourthon.backend.ticket.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MyTicketSummaryRepository
        extends JpaRepository<Ticket, Long> {

    // 티켓 수 및 지출 합계
    interface TicketTotals {
        Long getTicketCount();
        Long getTicketTotal();
        Long getCulturePassTotal();
        Long getMdTotal();
    }

    @Query("""
        SELECT
            COUNT(t) AS ticketCount,
            COALESCE(SUM(t.ticketPrice), 0) AS ticketTotal,
            COALESCE(SUM(t.culturePassAmount), 0) AS culturePassTotal,
            COALESCE(SUM(t.mdPrice), 0) AS mdTotal
        FROM Ticket t
        WHERE t.user.id = :userId
        """)
    TicketTotals findTotalsByUserId(
            @Param("userId") Long userId
    );

    // 실제 공연 장르별 티켓 수 집계
    @Query("""
        SELECT t.performance.genre, COUNT(t)
        FROM Ticket t
        WHERE t.user.id = :userId
        GROUP BY t.performance.genre
        """)
    List<Object[]> countTicketsByGenre(
            @Param("userId") Long userId
    );
}
