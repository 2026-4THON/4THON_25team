package com.fourthon.backend.kopis.client;

import com.fourthon.backend.common.client.RequestThrottle;
import com.fourthon.backend.kopis.config.KopisProperties;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RestKopisClientTest {

    private static final String BASE_URL = "http://kopis.test";
    private static final String BLOCKED = "<HTML><BODY><H1>Request Blocked</H1></BODY></HTML>";
    private static final String EMPTY_BOX_OFFICE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><boxofs></boxofs>";
    private static final LocalDate DATE = LocalDate.of(2026, 11, 16);

    private final RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final RestKopisClient client = new RestKopisClient(
            builder.build(),
            new RequestThrottle(Duration.ZERO),
            new KopisProperties(
                    "key",
                    BASE_URL,
                    0,
                    List.of(Duration.ZERO, Duration.ZERO),
                    new KopisProperties.Sync(false, DATE, 3, "1111", "UNI")
            )
    );

    @Test
    void 차단되면_기다렸다가_다시_요청한다() {
        server.expect(requestTo(startsWith(BASE_URL + "/boxoffice"))).andRespond(withBadRequest().body(BLOCKED));
        server.expect(requestTo(startsWith(BASE_URL + "/boxoffice")))
                .andRespond(withSuccess(EMPTY_BOX_OFFICE, MediaType.APPLICATION_XML));

        assertThat(client.fetchBoxOffice(DATE)).isEmpty();
        server.verify();
    }

    @Test
    void 다시_요청해도_계속_막히면_차단으로_멈춘다() {
        server.expect(requestTo(startsWith(BASE_URL + "/boxoffice"))).andRespond(withBadRequest().body(BLOCKED));
        server.expect(requestTo(startsWith(BASE_URL + "/boxoffice"))).andRespond(withBadRequest().body(BLOCKED));
        server.expect(requestTo(startsWith(BASE_URL + "/boxoffice"))).andRespond(withBadRequest().body(BLOCKED));

        assertThatThrownBy(() -> client.fetchBoxOffice(DATE)).isInstanceOf(KopisBlockedException.class);
        server.verify();
    }

    @Test
    void 차단이_아닌_오류는_다시_요청하지_않고_실패로_본다() {
        server.expect(requestTo(startsWith(BASE_URL + "/boxoffice")))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.fetchBoxOffice(DATE))
                .isInstanceOf(KopisException.class)
                .isNotInstanceOf(KopisBlockedException.class);
        server.verify();
    }
}
