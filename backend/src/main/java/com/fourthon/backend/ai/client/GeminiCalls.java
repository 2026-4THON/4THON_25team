package com.fourthon.backend.ai.client;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.function.Supplier;

final class GeminiCalls {

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration BACKOFF = Duration.ofSeconds(2);

    private GeminiCalls() {
    }

    static <T> T call(String action, Supplier<T> request) {
        for (int attempt = 1; ; attempt++) {
            try {
                return request.get();
            } catch (RestClientResponseException e) {
                boolean overloaded = e.getStatusCode().isSameCodeAs(HttpStatus.SERVICE_UNAVAILABLE);
                if (overloaded && attempt < MAX_ATTEMPTS) {
                    sleep(BACKOFF.multipliedBy(attempt));
                    continue;
                }
                if (overloaded || e.getStatusCode().isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS)) {
                    throw new AiUnavailableException(
                            "Gemini를 지금 쓸 수 없습니다(" + e.getStatusCode().value() + "): " + action, e);
                }
                throw new AiException(action + "에 실패했습니다: " + e.getStatusCode().value(), e);
            } catch (RestClientException e) {
                throw new AiException(action + "에 실패했습니다", e);
            }
        }
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiException("Gemini 재시도 대기 중 중단되었습니다", e);
        }
    }
}
