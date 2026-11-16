package com.fourthon.backend.taste.service;

import com.fourthon.backend.taste.domain.LifeWork;
import com.fourthon.backend.taste.domain.LifeWorkCategory;
import com.fourthon.backend.taste.domain.TagType;
import com.fourthon.backend.taste.domain.TasteAnalysis;
import com.fourthon.backend.taste.domain.TasteAnswers;
import com.fourthon.backend.taste.domain.TasteReport;
import com.fourthon.backend.taste.domain.TasteTag;
import com.fourthon.backend.taste.domain.UserTasteScore;
import com.fourthon.backend.taste.domain.VsChoice;
import com.fourthon.backend.taste.domain.VsQuestion;
import com.fourthon.backend.taste.dto.TasteRequest;
import com.fourthon.backend.taste.dto.TasteResponse;
import com.fourthon.backend.taste.repository.TasteReportRepository;
import com.fourthon.backend.taste.repository.UserTasteScoreRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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

@Service
@RequiredArgsConstructor
public class TasteService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final TasteReportRepository tasteReportRepository;
    private final UserTasteScoreRepository userTasteScoreRepository;
    private final TasteAnalyzer tasteAnalyzer;
    private final TasteScoreCalculator tasteScoreCalculator;
    private final TransactionTemplate transactionTemplate;

    public TasteResponse.Report getReport(Long userId) {
        return transactionTemplate.execute(status -> TasteResponse.Report.from(requireReport(userId)));
    }

    public TasteResponse.Report create(Long userId, TasteRequest.Save request) {
        if (tasteReportRepository.existsByUserId(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 취향 리포트가 있습니다");
        }
        TasteAnswers answers = toAnswers(request);
        TasteAnalysis analysis = tasteAnalyzer.analyze(answers);
        return transactionTemplate.execute(status -> {
            TasteReport report = tasteReportRepository.save(new TasteReport(userId, answers, analysis, now()));
            replaceScores(report);
            return TasteResponse.Report.from(report);
        });
    }

    public TasteResponse.Report update(Long userId, TasteRequest.Save request) {
        requireReport(userId);
        TasteAnswers answers = toAnswers(request);
        TasteAnalysis analysis = tasteAnalyzer.analyze(answers);
        return transactionTemplate.execute(status -> {
            TasteReport report = requireReport(userId);
            report.update(answers, analysis, now());
            replaceScores(report);
            return TasteResponse.Report.from(report);
        });
    }

    public void recalculateScores(Long userId) {
        transactionTemplate.executeWithoutResult(status ->
                tasteReportRepository.findByUserId(userId).ifPresent(this::replaceScores));
    }

    public Optional<String> findBriefing(Long userId) {
        return tasteReportRepository.findByUserId(userId).map(TasteReport::getBriefing);
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
                Map.of()
        );
        userTasteScoreRepository.saveAll(scores.entrySet().stream()
                .map(entry -> new UserTasteScore(userId, entry.getKey(), entry.getValue()))
                .toList());
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
