package com.fourthon.backend.ai.repository;

import com.fourthon.backend.ai.domain.ReviewAnalysisFailure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewAnalysisFailureRepository extends JpaRepository<ReviewAnalysisFailure, Long> {

    List<ReviewAnalysisFailure> findAllByOrderByFailedAtDesc();

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM ReviewAnalysisFailure f WHERE f.ticketId = :ticketId")
    void deleteByTicketId(@Param("ticketId") Long ticketId);
}
