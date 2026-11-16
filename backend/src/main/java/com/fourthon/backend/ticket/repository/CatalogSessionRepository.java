package com.fourthon.backend.ticket.repository;
import com.fourthon.backend.ticket.domain.CatalogSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface CatalogSessionRepository extends JpaRepository<CatalogSession,Long> {
    List<CatalogSession> findByPerformance_IdOrderByStartsAtAsc(Long performanceId);
    List<CatalogSession> findByPerformance_IdAndStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
        Long performanceId,LocalDateTime start,LocalDateTime end);
    Optional<CatalogSession> findByIdAndPerformance_Id(Long sessionId,Long performanceId);
}
