package com.fourthon.backend.show.service;

import com.fourthon.backend.show.domain.ShowDetail;
import com.fourthon.backend.show.dto.ShowSync;
import com.fourthon.backend.show.repository.ShowDetailRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowDetailService {

    private final ShowDetailRepository showDetailRepository;

    public Optional<ShowSync.Info> findSyncInfo(String kopisId) {
        return showDetailRepository.findByKopisId(kopisId)
                .map(detail -> new ShowSync.Info(
                        detail.getPerformanceId(),
                        detail.getKopisUpdatedAt(),
                        detail.getTimeGuide()
                ));
    }

    @Transactional
    public void save(ShowSync.Upsert command, LocalDateTime syncedAt) {
        showDetailRepository.findById(command.performanceId())
                .ifPresentOrElse(
                        detail -> detail.update(command, syncedAt),
                        () -> showDetailRepository.save(new ShowDetail(command, syncedAt))
                );
    }

    public List<ShowSync.SessionPlan> findSessionPlans(LocalDate today) {
        return showDetailRepository.findByEndDateGreaterThanEqual(today).stream()
                .map(detail -> new ShowSync.SessionPlan(
                        detail.getPerformanceId(),
                        detail.getStartDate(),
                        detail.getEndDate(),
                        detail.getTimeGuide()
                ))
                .toList();
    }

    public Map<String, Long> findPerformanceIds(Collection<String> kopisIds) {
        if (kopisIds.isEmpty()) {
            return Map.of();
        }
        return showDetailRepository.findByKopisIdIn(kopisIds).stream()
                .collect(Collectors.toMap(ShowDetail::getKopisId, ShowDetail::getPerformanceId));
    }
}
