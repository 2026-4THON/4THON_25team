
package com.fourthon.backend.my.repository;

import com.fourthon.backend.ticket.domain.TicketReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MyReviewSummaryRepository
        extends JpaRepository<TicketReview, Long> {

    @Query("""
        SELECT r.type, COUNT(r)
        FROM TicketReview r
        WHERE r.ticket.user.id = :userId
        GROUP BY r.type
        """)
    List<Object[]> countByTypeForUser(
            @Param("userId") Long userId
    );
}
