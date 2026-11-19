package com.fourthon.backend.show.service;

import com.fourthon.backend.show.client.VideoCandidate;
import com.fourthon.backend.show.config.YoutubeProperties;
import com.fourthon.backend.show.dto.ShowVideoQuery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class VideoScoreCalculator {

    private static final int TITLE_MATCH_SCORE = 2;
    private static final int OFFICIAL_KEYWORD_SCORE = 3;
    private static final int CURTAIN_CALL_SCORE = 2;
    private static final int RELATED_SCORE = 1;
    private static final int AUDIENCE_KEYWORD_PENALTY = -3;
    private static final int OLD_VIDEO_PENALTY = -2;
    private static final int OLD_VIDEO_YEARS = 1;
    private static final List<String> OFFICIAL_KEYWORDS = List.of(
            "하이라이트", "프레스콜", "티저", "스팟", "트레일러", "mv"
    );
    private static final String CURTAIN_CALL_KEYWORD = "커튼콜";
    private static final List<String> AUDIENCE_KEYWORDS = List.of(
            "리뷰", "후기", "브이로그", "반응", "리액션", "커버"
    );

    private final YoutubeProperties youtubeProperties;

    public List<String> select(ShowVideoQuery show, List<VideoCandidate> candidates) {
        String showTitle = normalize(stripRegion(show.title()));
        if (showTitle.isEmpty()) {
            return List.of();
        }
        return candidates.stream()
                .filter(candidate -> normalize(candidate.title()).contains(showTitle))
                .map(candidate -> new ScoredVideo(candidate.videoId(), score(show, candidate)))
                .filter(video -> video.score() >= youtubeProperties.minScore())
                .sorted(Comparator.comparingInt(ScoredVideo::score).reversed())
                .map(ScoredVideo::videoId)
                .distinct()
                .limit(youtubeProperties.maxVideos())
                .toList();
    }

    private int score(ShowVideoQuery show, VideoCandidate candidate) {
        String title = normalize(candidate.title());
        int score = TITLE_MATCH_SCORE;
        if (containsAny(title, OFFICIAL_KEYWORDS)) {
            score += OFFICIAL_KEYWORD_SCORE;
        } else if (title.contains(CURTAIN_CALL_KEYWORD)) {
            score += CURTAIN_CALL_SCORE;
        }
        if (isRelated(show, candidate, title)) {
            score += RELATED_SCORE;
        }
        if (containsAny(title, AUDIENCE_KEYWORDS)) {
            score += AUDIENCE_KEYWORD_PENALTY;
        }
        if (isOld(show.startDate(), candidate.publishedDate())) {
            score += OLD_VIDEO_PENALTY;
        }
        return score;
    }

    private boolean isRelated(ShowVideoQuery show, VideoCandidate candidate, String title) {
        String venue = normalize(stripRegion(show.venueName()));
        if (!venue.isEmpty() && normalize(candidate.channelTitle()).contains(venue)) {
            return true;
        }
        return show.actorNames().stream()
                .map(VideoScoreCalculator::normalize)
                .filter(name -> !name.isEmpty())
                .anyMatch(title::contains);
    }

    private boolean isOld(LocalDate startDate, LocalDate publishedDate) {
        if (startDate == null || publishedDate == null) {
            return false;
        }
        return publishedDate.isBefore(startDate.minusYears(OLD_VIDEO_YEARS));
    }

    private boolean containsAny(String text, List<String> keywords) {
        return keywords.stream().anyMatch(text::contains);
    }

    private static String stripRegion(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\\[[^]]*]|\\([^)]*\\)", "");
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[^\\p{L}\\p{N}]", "").toLowerCase(Locale.ROOT);
    }

    private record ScoredVideo(String videoId, int score) {
    }
}
