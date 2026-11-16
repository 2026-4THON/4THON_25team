package com.fourthon.backend.show.repository;

import com.fourthon.backend.show.domain.BoxOfficeRank;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface BoxOfficeRankRepository extends JpaRepository<BoxOfficeRank, Long> {

    @Modifying
    @Query("DELETE FROM BoxOfficeRank r WHERE r.rankedDate = :rankedDate")
    void deleteByRankedDate(@Param("rankedDate") LocalDate rankedDate);
}
