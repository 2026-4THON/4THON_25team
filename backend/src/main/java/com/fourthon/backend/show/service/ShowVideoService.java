package com.fourthon.backend.show.service;

import com.fourthon.backend.common.client.RequestThrottle;
import com.fourthon.backend.show.client.VideoCandidate;
import com.fourthon.backend.show.client.YoutubeClient;
import com.fourthon.backend.show.client.YoutubeQuotaExceededException;
import com.fourthon.backend.show.config.YoutubeProperties;
import com.fourthon.backend.show.domain.ShowDetail;
import com.fourthon.backend.show.dto.ShowVideoQuery;
import com.fourthon.backend.show.repository.CatalogPerformanceCastRepository;
import com.fourthon.backend.show.repository.ShowDetailRepository;
import com.fourthon.backend.ticket.domain.CatalogPerformance;
import com.fourthon.backend.ticket.repository.CatalogPerformanceRepository;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShowVideoService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final String BRACKETED_TEXT = "\\[[^]]*]|\\([^)]*\\)";
    private static final Map<CatalogPerformance.Genre, String> GENRE_KEYWORDS = Map.of(
            CatalogPerformance.Genre.MUSICAL, "뮤지컬",
            CatalogPerformance.Genre.PLAY, "연극"
    );

    private final ShowDetailRepository showDetailRepository;
    private final CatalogPerformanceRepository catalogPerformanceRepository;
    private final CatalogPerformanceCastRepository catalogPerformanceCastRepository;
    private final YoutubeClient youtubeClient;
    private final VideoScoreCalculator videoScoreCalculator;
    private final YoutubeProperties youtubeProperties;
    private final RequestThrottle youtubeThrottle;
    private final TransactionTemplate transactionTemplate;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ExecutorService runner = Executors.newSingleThreadExecutor(
            runnable -> new Thread(runnable, "show-video")
    );

    public boolean isRunning() {
        return running.get();
    }

    public void runInBackground() {
        runner.execute(this::run);
    }

    public void run() {
        if (!running.compareAndSet(false, true)) {
            log.info("공연 영상 검색이 이미 실행 중이라 건너뜀");
            return;
        }
        try {
            if (!youtubeClient.isConfigured()) {
                log.warn("YOUTUBE_API_KEY가 없어 공연 영상 검색을 건너뜀");
                return;
            }
            searchTargets();
        } catch (RuntimeException e) {
            log.error("공연 영상 검색 실패", e);
        } finally {
            running.set(false);
        }
    }

    private void searchTargets() {
        List<Long> targets = showDetailRepository.findVideoSearchTargets(
                LocalDate.now(SEOUL),
                PageRequest.of(0, youtubeProperties.dailyLimit())
        );
        log.info("공연 영상 검색 시작: 대상 {}건", targets.size());
        VideoSearchCount count = new VideoSearchCount();
        for (Long performanceId : targets) {
            try {
                searchOne(performanceId, count);
            } catch (YoutubeQuotaExceededException e) {
                log.error("YouTube 검색 할당량을 모두 써서 공연 영상 검색을 중단합니다. 남은 공연은 다음 실행에서 이어서 검색합니다", e);
                break;
            } catch (RuntimeException e) {
                count.failed++;
                log.warn("공연 영상 검색 실패: performanceId={}", performanceId, e);
            }
        }
        log.info("공연 영상 검색 종료: 영상 있음 {}, 영상 없음 {}, 실패 {}, 남음 {}",
                count.found, count.none, count.failed,
                targets.size() - count.found - count.none - count.failed);
    }

    private void searchOne(Long performanceId, VideoSearchCount count) {
        Optional<ShowDetail> detail = showDetailRepository.findById(performanceId);
        Optional<CatalogPerformance> performance = catalogPerformanceRepository.findById(performanceId);
        if (detail.isEmpty() || performance.isEmpty()) {
            count.failed++;
            return;
        }
        ShowVideoQuery query = new ShowVideoQuery(
                performance.get().getTitle(),
                performance.get().getVenue(),
                actorNames(performanceId),
                detail.get().getStartDate()
        );
        youtubeThrottle.acquire();
        List<VideoCandidate> candidates = youtubeClient.search(
                searchKeyword(performance.get()),
                youtubeProperties.candidateCount()
        );
        List<String> videoIds = videoScoreCalculator.select(query, candidates);
        LocalDateTime now = LocalDateTime.now(SEOUL);
        transactionTemplate.executeWithoutResult(status -> showDetailRepository.findById(performanceId)
                .ifPresent(found -> found.completeVideoSearch(videoIds, now)));
        if (videoIds.isEmpty()) {
            count.none++;
            return;
        }
        count.found++;
    }

    private List<String> actorNames(Long performanceId) {
        return catalogPerformanceCastRepository.findCastWithActors(performanceId).stream()
                .map(member -> member.getActor().getName())
                .toList();
    }

    private String searchKeyword(CatalogPerformance performance) {
        String title = performance.getTitle().replaceAll(BRACKETED_TEXT, "").strip();
        String genreKeyword = GENRE_KEYWORDS.get(performance.getGenre());
        if (genreKeyword == null) {
            return title;
        }
        return title + " " + genreKeyword;
    }

    @PreDestroy
    public void shutdown() {
        runner.shutdownNow();
    }

    private static class VideoSearchCount {

        private int found;
        private int none;
        private int failed;
    }
}
