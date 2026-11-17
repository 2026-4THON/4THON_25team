package com.fourthon.backend.show.service;

import com.fourthon.backend.show.domain.BoxOfficeRank;
import com.fourthon.backend.show.dto.ShowSync;
import com.fourthon.backend.show.repository.BoxOfficeRankRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoxOfficeService {

    private final BoxOfficeRankRepository boxOfficeRankRepository;

    @Transactional
    public void replaceRanks(LocalDate rankedDate, List<ShowSync.Rank> ranks) {
        boxOfficeRankRepository.deleteByRankedDate(rankedDate);
        List<BoxOfficeRank> rows = ranks.stream()
                .map(rank -> new BoxOfficeRank(rankedDate, rank.performanceId(), rank.rank()))
                .toList();
        boxOfficeRankRepository.saveAll(rows);
    }
}
