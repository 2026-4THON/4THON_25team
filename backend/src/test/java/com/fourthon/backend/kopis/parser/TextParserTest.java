package com.fourthon.backend.kopis.parser;

import com.fourthon.backend.ticket.domain.CatalogPerformance;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextParserTest {

    private final RuntimeParser runtimeParser = new RuntimeParser();
    private final AgeParser ageParser = new AgeParser();
    private final CastParser castParser = new CastParser();
    private final GenreMapper genreMapper = new GenreMapper();

    @Test
    void 시간과_분을_분으로_바꾼다() {
        assertThat(runtimeParser.parseMinutes("1시간 30분")).isEqualTo(90);
        assertThat(runtimeParser.parseMinutes("2시간")).isEqualTo(120);
        assertThat(runtimeParser.parseMinutes("100분")).isEqualTo(100);
        assertThat(runtimeParser.parseMinutes("1시간 40분 (인터미션 15분 포함)")).isEqualTo(100);
    }

    @Test
    void 러닝타임을_모르면_0이다() {
        assertThat(runtimeParser.parseMinutes("")).isZero();
        assertThat(runtimeParser.parseMinutes("미정")).isZero();
    }

    @Test
    void 관람_연령을_읽는다() {
        assertThat(ageParser.parseMinAge("만 13세 이상")).isEqualTo(13);
        assertThat(ageParser.parseMinAge("48개월 이상")).isEqualTo(4);
        assertThat(ageParser.parseMinAge("전체 관람가")).isZero();
        assertThat(ageParser.parseMinAge("청소년 관람불가")).isEqualTo(19);
    }

    @Test
    void 출연진_이름을_나눈다() {
        assertThat(castParser.parseNames("김철수, 이영희, 박민수 등"))
                .containsExactly("김철수", "이영희", "박민수");
    }

    @Test
    void 중복과_빈_이름은_뺀다() {
        assertThat(castParser.parseNames("김철수, , 김철수"))
                .containsExactly("김철수");
        assertThat(castParser.parseNames(" ")).isEmpty();
    }

    @Test
    void KOPIS_장르를_세_가지로_묶는다() {
        assertThat(genreMapper.toGenre("뮤지컬")).isEqualTo(CatalogPerformance.Genre.MUSICAL);
        assertThat(genreMapper.toGenre("연극")).isEqualTo(CatalogPerformance.Genre.PLAY);
        assertThat(genreMapper.toGenre("서커스/마술")).isEqualTo(CatalogPerformance.Genre.ETC);
        assertThat(genreMapper.toGenre(null)).isEqualTo(CatalogPerformance.Genre.ETC);
    }
}
