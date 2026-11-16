package com.fourthon.backend.show.repository;

import com.fourthon.backend.show.domain.ShowDetail;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ShowDetailRepository extends JpaRepository<ShowDetail, Long> {

    Optional<ShowDetail> findByKopisId(String kopisId);

    List<ShowDetail> findByKopisIdIn(Collection<String> kopisIds);

    List<ShowDetail> findByEndDateGreaterThanEqual(LocalDate date);
}
