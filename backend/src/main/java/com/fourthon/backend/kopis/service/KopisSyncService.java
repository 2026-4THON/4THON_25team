package com.fourthon.backend.kopis.service;

import com.fourthon.backend.kopis.client.KopisBlockedException;
import com.fourthon.backend.kopis.client.KopisClient;
import com.fourthon.backend.kopis.client.KopisResponse;
import com.fourthon.backend.kopis.config.KopisProperties;
import com.fourthon.backend.kopis.domain.KopisExcludedShow;
import com.fourthon.backend.kopis.domain.SyncState;
import com.fourthon.backend.kopis.parser.SessionParser;
import com.fourthon.backend.kopis.repository.KopisExcludedShowRepository;
import com.fourthon.backend.kopis.repository.SyncStateRepository;
import com.fourthon.backend.show.dto.ShowSync;
import com.fourthon.backend.show.service.BoxOfficeService;
import com.fourthon.backend.show.service.RatingPrior;
import com.fourthon.backend.show.service.ShowDetailService;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KopisSyncService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final String SHOW_SYNC = "KOPIS_SHOWS";
    private static final int PAGE_SIZE = 100;
    private static final int MAX_RANGE_DAYS = 31;

    private final KopisClient kopisClient;
    private final ShowImporter showImporter;
    private final CatalogWriter catalogWriter;
    private final SessionParser sessionParser;
    private final SyncStateRepository syncStateRepository;
    private final KopisExcludedShowRepository kopisExcludedShowRepository;
    private final ShowDetailService showDetailService;
    private final BoxOfficeService boxOfficeService;
    private final RatingPrior ratingPrior;
    private final KopisProperties kopisProperties;
    private final TransactionTemplate transactionTemplate;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ExecutorService manualRunner = Executors.newSingleThreadExecutor(
            runnable -> new Thread(runnable, "kopis-sync")
    );

    public boolean isRunning() {
        return running.get();
    }

    public void runDailyInBackground() {
        manualRunner.execute(this::runDaily);
    }

    public void runDaily() {
        if (!running.compareAndSet(false, true)) {
            log.info("KOPIS 동기화가 이미 실행 중이라 건너뜀");
            return;
        }
        try {
            LocalDateTime now = LocalDateTime.now(SEOUL);
            LocalDate today = now.toLocalDate();
            boolean completed = syncShows(now);
            extendSessions(today);
            if (completed) {
                syncBoxOffice(today.minusDays(1));
            }
            ratingPrior.refresh();
        } catch (RuntimeException e) {
            log.error("KOPIS 동기화 실패", e);
        } finally {
            running.set(false);
        }
    }

    private boolean syncShows(LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        SyncState state = syncStateRepository.findById(SHOW_SYNC)
                .orElseGet(() -> new SyncState(SHOW_SYNC));
        boolean firstSync = state.getLastSuccessDate() == null;
        Set<String> kopisIds = collectKopisIds(today, state.getLastSuccessDate());
        Set<String> targets = selectTargets(kopisIds, firstSync);
        log.info("KOPIS 공연 동기화 시작: 목록 {}건 중 요청 대상 {}건, 기준일 {}",
                kopisIds.size(), targets.size(), state.getLastSuccessDate());

        SyncCount count = new SyncCount();
        boolean blocked = false;
        for (String kopisId : targets) {
            try {
                importOne(kopisId, now, count);
            } catch (KopisBlockedException e) {
                blocked = true;
                log.error("KOPIS가 요청을 막아 동기화를 중단합니다. 남은 공연은 다음 실행에서 이어서 받습니다", e);
                break;
            } catch (RuntimeException e) {
                count.failed++;
                log.warn("KOPIS 공연 동기화 실패: {}", kopisId, e);
            }
        }
        int processed = count.created + count.updated + count.skipped + count.notDaehakro + count.failed;
        log.info("KOPIS 공연 동기화 종료: 추가 {}, 갱신 {}, 변경 없음 {}, 대학로 아님 {}, 실패 {}, 남음 {}",
                count.created, count.updated, count.skipped, count.notDaehakro, count.failed,
                targets.size() - processed);

        if (!blocked && count.failed == 0) {
            state.markSuccess(today);
            syncStateRepository.save(state);
        }
        return !blocked;
    }

    private Set<String> selectTargets(Set<String> kopisIds, boolean firstSync) {
        Set<String> targets = new LinkedHashSet<>(kopisIds);
        targets.removeAll(kopisExcludedShowRepository.findAllById(kopisIds).stream()
                .map(KopisExcludedShow::getKopisId)
                .collect(Collectors.toSet()));
        if (firstSync) {
            targets.removeAll(showDetailService.findPerformanceIds(kopisIds).keySet());
        }
        return targets;
    }

    private void importOne(String kopisId, LocalDateTime now, SyncCount count) {
        KopisResponse.PerformanceDetail detail = kopisClient.fetchPerformanceDetail(kopisId)
                .orElse(null);
        if (detail == null) {
            count.notDaehakro++;
            return;
        }
        if (!detail.daehakro()) {
            kopisExcludedShowRepository.save(new KopisExcludedShow(kopisId, now));
            count.notDaehakro++;
            return;
        }
        ShowImporter.ImportResult result = transactionTemplate.execute(
                status -> showImporter.importShow(detail, now)
        );
        count.add(result);
    }

    private Set<String> collectKopisIds(LocalDate today, LocalDate updatedAfter) {
        LocalDate start = kopisProperties.sync().startDate();
        LocalDate end = today.plusMonths(kopisProperties.sync().futureMonths());
        Set<String> kopisIds = new LinkedHashSet<>();
        for (LocalDate from = start; !from.isAfter(end); from = from.plusDays(MAX_RANGE_DAYS)) {
            LocalDate to = from.plusDays(MAX_RANGE_DAYS - 1);
            collectRange(from, to.isAfter(end) ? end : to, updatedAfter, kopisIds);
        }
        return kopisIds;
    }

    private void collectRange(
            LocalDate from,
            LocalDate to,
            LocalDate updatedAfter,
            Set<String> kopisIds) {

        for (int page = 1; ; page++) {
            List<KopisResponse.Performance> performances =
                    kopisClient.fetchPerformances(from, to, updatedAfter, page, PAGE_SIZE);
            performances.forEach(performance -> kopisIds.add(performance.kopisId()));
            if (performances.size() < PAGE_SIZE) {
                return;
            }
        }
    }

    private void extendSessions(LocalDate today) {
        LocalDate limit = today.plusMonths(kopisProperties.sync().futureMonths());
        int failed = 0;
        for (ShowSync.SessionPlan plan : showDetailService.findSessionPlans(today)) {
            LocalDate from = plan.startDate().isAfter(today) ? plan.startDate() : today;
            LocalDate to = plan.endDate().isAfter(limit) ? limit : plan.endDate();
            try {
                transactionTemplate.executeWithoutResult(status -> catalogWriter.syncSessions(
                        plan.performanceId(),
                        sessionParser.generate(plan.timeGuide(), from, to),
                        today.atStartOfDay()
                ));
            } catch (RuntimeException e) {
                failed++;
                log.warn("회차 연장 실패: performanceId={}", plan.performanceId(), e);
            }
        }
        log.info("회차 연장 종료: 실패 {}", failed);
    }

    private void syncBoxOffice(LocalDate rankedDate) {
        List<KopisResponse.BoxOffice> boxOffices = kopisClient.fetchBoxOffice(rankedDate);
        Map<String, Long> performanceIds = showDetailService.findPerformanceIds(
                boxOffices.stream().map(KopisResponse.BoxOffice::kopisId).toList()
        );
        Set<Long> ranked = new LinkedHashSet<>();
        List<ShowSync.Rank> ranks = boxOffices.stream()
                .filter(boxOffice -> performanceIds.containsKey(boxOffice.kopisId()))
                .map(boxOffice -> new ShowSync.Rank(performanceIds.get(boxOffice.kopisId()), boxOffice.rank()))
                .filter(rank -> ranked.add(rank.performanceId()))
                .toList();
        boxOfficeService.replaceRanks(rankedDate, ranks);
        log.info("예매 순위 저장: {} {}건", rankedDate, ranks.size());
    }

    @PreDestroy
    public void shutdown() {
        manualRunner.shutdownNow();
    }

    private static class SyncCount {

        private int created;
        private int updated;
        private int skipped;
        private int notDaehakro;
        private int failed;

        private void add(ShowImporter.ImportResult result) {
            if (result == ShowImporter.ImportResult.CREATED) {
                created++;
            } else if (result == ShowImporter.ImportResult.UPDATED) {
                updated++;
            } else {
                skipped++;
            }
        }
    }
}
