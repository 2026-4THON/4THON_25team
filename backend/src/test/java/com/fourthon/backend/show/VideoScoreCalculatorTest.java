package com.fourthon.backend.show;

import com.fourthon.backend.show.client.VideoCandidate;
import com.fourthon.backend.show.config.YoutubeProperties;
import com.fourthon.backend.show.dto.ShowVideoQuery;
import com.fourthon.backend.show.service.VideoScoreCalculator;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VideoScoreCalculatorTest {

    private static final LocalDate START_DATE = LocalDate.of(2026, 10, 1);
    private static final LocalDate RECENT = LocalDate.of(2026, 9, 1);

    private final VideoScoreCalculator videoScoreCalculator = new VideoScoreCalculator(
            new YoutubeProperties(null, "https://www.googleapis.com/youtube/v3", 10, 2, 3, 90, 1000, true)
    );
    private final ShowVideoQuery show = new ShowVideoQuery("빨래 [대학로]", "학전블루 소극장", List.of("홍길동"), START_DATE);

    @Test
    void 공연명이_제목에_없으면_제외한다() {
        List<String> videoIds = videoScoreCalculator.select(show, List.of(
                candidate("a", "다른 뮤지컬 하이라이트", "공연채널", RECENT)
        ));

        assertThat(videoIds).isEmpty();
    }

    @Test
    void 리뷰_영상보다_하이라이트를_먼저_고른다() {
        List<String> videoIds = videoScoreCalculator.select(show, List.of(
                candidate("review", "뮤지컬 빨래 관람 후기", "개인채널", RECENT),
                candidate("highlight", "뮤지컬 <빨래> 2026 하이라이트", "공연채널", RECENT)
        ));

        assertThat(videoIds).containsExactly("highlight");
    }

    @Test
    void 기준_점수_미만이면_빈_목록이다() {
        List<String> videoIds = videoScoreCalculator.select(show, List.of(
                candidate("plain", "빨래", "개인채널", RECENT)
        ));

        assertThat(videoIds).isEmpty();
    }

    @Test
    void 점수가_높은_순으로_최대_개수까지만_고른다() {
        List<String> videoIds = videoScoreCalculator.select(show, List.of(
                candidate("teaser", "빨래 티저", "공연채널", RECENT),
                candidate("actor", "빨래 프레스콜 홍길동", "공연채널", RECENT),
                candidate("spot", "빨래 스팟 영상", "공연채널", RECENT)
        ));

        assertThat(videoIds).containsExactly("actor", "teaser");
    }

    @Test
    void 채널명에_공연장명이_있으면_가산한다() {
        List<String> videoIds = videoScoreCalculator.select(show, List.of(
                candidate("venue", "빨래 공연 영상", "학전블루소극장", RECENT)
        ));

        assertThat(videoIds).containsExactly("venue");
    }

    @Test
    void 개막보다_1년_넘게_먼저_올라온_영상은_감점한다() {
        List<String> videoIds = videoScoreCalculator.select(show, List.of(
                candidate("old", "빨래 커튼콜", "공연채널", LocalDate.of(2024, 1, 1))
        ));

        assertThat(videoIds).isEmpty();
    }

    @Test
    void 공식_영상을_커튼콜_영상보다_먼저_고른다() {
        List<String> videoIds = videoScoreCalculator.select(show, List.of(
                candidate("curtain1", "빨래 커튼콜", "개인채널", RECENT),
                candidate("curtain2", "빨래 커튼콜 영상", "개인채널", RECENT),
                candidate("official", "빨래 하이라이트", "공연채널", RECENT)
        ));

        assertThat(videoIds).containsExactly("official", "curtain1");
    }

    @Test
    void 커튼콜_영상도_기준_점수를_넘으면_고른다() {
        List<String> videoIds = videoScoreCalculator.select(show, List.of(
                candidate("curtain", "빨래 커튼콜", "개인채널", RECENT)
        ));

        assertThat(videoIds).containsExactly("curtain");
    }

    private VideoCandidate candidate(String videoId, String title, String channelTitle, LocalDate publishedDate) {
        return new VideoCandidate(videoId, title, channelTitle, publishedDate);
    }
}
