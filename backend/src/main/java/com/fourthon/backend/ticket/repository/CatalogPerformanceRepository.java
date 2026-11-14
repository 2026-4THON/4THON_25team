package com.fourthon.backend.ticket.repository;
import com.fourthon.backend.ticket.domain.CatalogPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface CatalogPerformanceRepository extends JpaRepository<CatalogPerformance,Long> {
    List<CatalogPerformance> findTop30ByTitleContainingIgnoreCaseOrderByTitleAsc(String keyword);
}
