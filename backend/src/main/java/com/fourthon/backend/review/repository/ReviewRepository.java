
package com.fourthon.backend.review.repository;

import com.fourthon.backend.ticket.domain.TicketReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReviewRepository
        extends JpaRepository<TicketReview, Long>,
        JpaSpecificationExecutor<TicketReview> {
}
