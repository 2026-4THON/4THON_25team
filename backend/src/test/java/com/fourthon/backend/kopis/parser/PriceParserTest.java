package com.fourthon.backend.kopis.parser;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PriceParserTest {

    private final PriceParser priceParser = new PriceParser();

    @Test
    void 좌석_등급별_가격을_나눈다() {
        List<PriceParser.SeatPrice> prices = priceParser.parse("R석 77,000원, S석 66,000원");

        assertThat(prices).containsExactly(
                new PriceParser.SeatPrice("R석", 77000),
                new PriceParser.SeatPrice("S석", 66000)
        );
    }

    @Test
    void 전석_가격을_읽는다() {
        assertThat(priceParser.parse("전석 30,000원"))
                .containsExactly(new PriceParser.SeatPrice("전석", 30000));
    }

    @Test
    void 등급_없는_가격은_전석으로_본다() {
        assertThat(priceParser.parse("30,000원"))
                .containsExactly(new PriceParser.SeatPrice("전석", 30000));
    }

    @Test
    void 무료_공연은_0원이다() {
        assertThat(priceParser.parse("무료"))
                .containsExactly(new PriceParser.SeatPrice("전석", 0));
    }

    @Test
    void 빈_문구는_빈_목록이다() {
        assertThat(priceParser.parse(" ")).isEmpty();
        assertThat(priceParser.parse(null)).isEmpty();
    }

    @Test
    void 최저가를_구한다() {
        List<PriceParser.SeatPrice> prices = priceParser.parse("VIP석 110,000원, R석 88,000원, S석 66,000원");

        assertThat(priceParser.minPrice(prices)).hasValue(66000);
    }
}
