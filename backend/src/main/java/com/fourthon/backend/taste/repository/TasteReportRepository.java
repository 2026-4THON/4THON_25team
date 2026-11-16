package com.fourthon.backend.taste.repository;

import com.fourthon.backend.taste.domain.TasteReport;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TasteReportRepository extends JpaRepository<TasteReport, Long> {

    Optional<TasteReport> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
