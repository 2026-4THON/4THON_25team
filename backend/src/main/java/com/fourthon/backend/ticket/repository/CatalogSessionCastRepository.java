package com.fourthon.backend.ticket.repository;
import com.fourthon.backend.ticket.domain.CatalogSessionCast;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface CatalogSessionCastRepository extends JpaRepository<CatalogSessionCast,Long> {
    List<CatalogSessionCast> findBySession_IdOrderByCastOrderAsc(Long sessionId);
}
