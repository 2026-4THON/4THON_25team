
package com.fourthon.backend.my.repository;

import com.fourthon.backend.ticket.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MyTicketSummaryRepository
        extends JpaRepository<Ticket, Long> {

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
}
