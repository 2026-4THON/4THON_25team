package com.fourthon.backend.ticket.repository;
import com.fourthon.backend.ticket.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;


public interface TicketRepository extends JpaRepository<Ticket,Long> {
    List<Ticket> findByUser_IdOrderByViewedAtAscIdAsc(Long userId);
    Optional<Ticket> findByIdAndUser_Id(Long id,Long userId);
}
