package com.fourthon.backend.taste.repository;

import com.fourthon.backend.taste.domain.UserTasteScore;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserTasteScoreRepository extends JpaRepository<UserTasteScore, Long> {

    List<UserTasteScore> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM UserTasteScore s WHERE s.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
