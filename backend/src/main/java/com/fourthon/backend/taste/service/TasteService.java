package com.fourthon.backend.taste.service;

import com.fourthon.backend.common.domain.TagType;
import com.fourthon.backend.common.domain.TasteTag;
import com.fourthon.backend.show.service.ShowTagScoreService;
import com.fourthon.backend.taste.domain.LifeWork;
import com.fourthon.backend.taste.domain.LifeWorkCategory;
import com.fourthon.backend.taste.domain.TasteAnalysis;
import com.fourthon.backend.taste.domain.TasteAnswers;
import com.fourthon.backend.taste.domain.TasteReport;
import com.fourthon.backend.taste.domain.TicketRating;
import com.fourthon.backend.taste.domain.UserTasteScore;
import com.fourthon.backend.taste.domain.VsChoice;
import com.fourthon.backend.taste.domain.VsQuestion;
import com.fourthon.backend.taste.dto.TasteRequest;
import com.fourthon.backend.taste.dto.TasteResponse;
import com.fourthon.backend.taste.event.TasteReportSavedEvent;
import com.fourthon.backend.taste.repository.TasteReportRepository;
import com.fourthon.backend.taste.repository.TicketRatingRepository;
import com.fourthon.backend.taste.repository.UserTasteScoreRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TasteService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final TasteReportRepository tasteReportRepository;
    private final UserTasteScoreRepository userTasteScoreRepository;
    private final TasteAnalyzer tasteAnalyzer;
    private final TasteScoreCalculator tasteScoreCalculator;
    private final RatingHistoryCalculator ratingHistoryCalculator;
    private final TicketRatingRepository ticketRatingRepository;
    private final ShowTagScoreService showTagScoreService;
    private final TransactionTemplate transactionTemplate;
    private final ApplicationEventPublisher eventPublisher;

    public TasteResponse.Report getReport(Long userId) {
        return transactionTemplate.execute(status -> TasteResponse.Report.from(requireReport(userId)));
    }

    public TasteResponse.Report create(Long userId, TasteRequest.Save request) {
        if (tasteReportRepository.existsByUserId(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 취향 리포트가 있습니다");
        }
        TasteAnswers answers = toAnswers(request);
        TasteAnalysis analysis = tasteAnalyzer.analyze(answers);
        TasteResponse.Report saved = transactionTemplate.execute(status -> {
            TasteReport report = tasteReportRepository.save(new TasteReport(userId, answers, analysis, now()));
            replaceScores(report);
            return TasteResponse.Report.from(report);
        });
        eventPublisher.publishEvent(new TasteReportSavedEvent(userId, false));
        return saved;
    }

    public TasteResponse.Report update(Long userId, TasteRequest.Save request) {
        requireReport(userId);
        TasteAnswers answers = toAnswers(request);
        TasteAnalysis analysis = tasteAnalyzer.analyze(answers);
        TasteResponse.Report updated = transactionTemplate.execute(status -> {
            TasteReport report = requireReport(userId);
            report.update(answers, analysis, now());
            replaceScores(report);
            return TasteResponse.Report.from(report);
        });
        eventPublisher.publishEvent(new TasteReportSavedEvent(userId, true));
        return updated;
    }

    public void recalculateScores(Long userId) {
        TransactionTemplate requiresNew = new TransactionTemplate(transactionTemplate.getTransactionManager());
        requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        requiresNew.executeWithoutResult(status ->
                tasteReportRepository.findByUserId(userId).ifPresent(this::replaceScores));
    }

    public List<Long> findUserIdsWithReport() {
        return tasteReportRepository.findAllUserIds();
    }

    public Optional<String> findBriefing(Long userId) {
        return tasteReportRepository.findByUserId(userId).map(TasteReport::getBriefing);
    }

    public List<String> findLifeWorkTitles(Long userId) {
        List<String> titles = transactionTemplate.execute(status -> tasteReportRepository.findByUserId(userId)
                .map(report -> report.getLifeWorks().stream()
                        .map(LifeWork::getTitle)
                        .toList())
                .orElse(List.of()));
        return titles == null ? List.of() : titles;
    }

    public Map<TasteTag, Double> findScores(Long userId) {
        Map<TasteTag, Double> scores = new EnumMap<>(TasteTag.class);
        userTasteScoreRepository.findByUserId(userId)
                .forEach(score -> scores.put(score.getTag(), score.getScore()));
        return scores;
    }

    private void replaceScores(TasteReport report) {
        Long userId = report.getUserId();
        userTasteScoreRepository.deleteByUserId(userId);
        Map<TasteTag, Double> scores = tasteScoreCalculator.calculate(
                report.selectedTagSet(),
                report.aiScoreMap(),
                ratingHistory(userId)
        );
        userTasteScoreRepository.saveAll(scores.entrySet().stream()
                .map(entry -> new UserTasteScore(userId, entry.getKey(), entry.getValue()))
                .toList());
    }

    private Map<TasteTag, Double> ratingHistory(Long userId) {
        List<TicketRating> ratings = ticketRatingRepository.findByUserIds(List.of(userId)).stream()
                .map(row -> TicketRating.ofHalfUnits(row.getUserId(), row.getPerformanceId(), row.getRatingHalfUnits()))
                .toList();
        if (ratings.isEmpty()) {
            return Map.of();
        }
        return ratingHistoryCalculator.historyScores(
                ratings,
                showTagScoreService.findScores(ratings.stream()
                        .map(TicketRating::performanceId)
                        .collect(Collectors.toSet()))
        );
    }

    private TasteReport requireReport(Long userId) {
        return tasteReportRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "취향 리포트가 없습니다"
                ));
    }

    private TasteAnswers toAnswers(TasteRequest.Save request) {
        Set<TasteTag> tags = EnumSet.noneOf(TasteTag.class);
        request.genres().forEach(label -> tags.add(toTag(label, TagType.GENRE)));
        request.moods().forEach(label -> tags.add(toTag(label, TagType.MOOD)));
        List<LifeWork> lifeWorks = request.lifeWorks().stream()
                .map(input -> new LifeWork(toCategory(input.category()), input.title()))
                .toList();
        return new TasteAnswers(tags, lifeWorks, toVsChoices(request.vsChoices()), request.freeText().trim());
    }

    private TasteTag toTag(String label, TagType type) {
        return TasteTag.fromLabel(label, type)
                .orElseThrow(() -> bad("알 수 없는 " + (type == TagType.GENRE ? "장르" : "분위기") + "입니다: " + label));
    }

    private LifeWorkCategory toCategory(String key) {
        return LifeWorkCategory.fromKey(key)
                .orElseThrow(() -> bad("알 수 없는 인생작 카테고리입니다: " + key));
    }

    private List<VsChoice> toVsChoices(List<String> choices) {
        Set<String> answeredCodes = new HashSet<>();
        List<VsChoice> vsChoices = choices.stream()
                .map(choice -> {
                    VsQuestion question = VsQuestion.findByChoice(choice)
                            .orElseThrow(() -> bad("알 수 없는 VS 선택지입니다: " + choice));
                    if (!answeredCodes.add(question.getCode())) {
                        throw bad("VS 문항마다 하나만 고를 수 있습니다");
                    }
                    return new VsChoice(question.getCode(), choice);
                })
                .toList();
        if (answeredCodes.size() != VsQuestion.values().length) {
            throw bad("VS 문항 " + VsQuestion.values().length + "개에 모두 답해야 합니다");
        }
        return vsChoices;
    }

    private ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(SEOUL);
    }
}
