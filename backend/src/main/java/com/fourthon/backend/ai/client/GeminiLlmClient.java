package com.fourthon.backend.ai.client;

import com.fourthon.backend.ai.config.GeminiProperties;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class GeminiLlmClient implements LlmClient {

    private static final String API_KEY_HEADER = "x-goog-api-key";
    private static final String JSON_MIME_TYPE = "application/json";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(60);

    private final RestClient restClient;
    private final GeminiProperties geminiProperties;

    public GeminiLlmClient(GeminiProperties geminiProperties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        this.restClient = RestClient.builder()
                .baseUrl(geminiProperties.baseUrl())
                .requestFactory(requestFactory)
                .build();
        this.geminiProperties = geminiProperties;
    }

    @Override
    public boolean isConfigured() {
        String apiKey = geminiProperties.apiKey();
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public String generateJson(String prompt) {
        if (!isConfigured()) {
            throw new AiException("GEMINI_API_KEY가 설정되지 않았습니다");
        }
        String apiKey = geminiProperties.apiKey();
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "responseMimeType", JSON_MIME_TYPE,
                        "temperature", geminiProperties.temperature()
                )
        );
        try {
            GenerateResponse response = restClient.post()
                    .uri("/models/{model}:generateContent", geminiProperties.model())
                    .header(API_KEY_HEADER, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(GenerateResponse.class);
            return stripCodeFence(extractText(response));
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS)) {
                throw new AiRateLimitException("Gemini 요청 한도를 넘었습니다", e);
            }
            throw new AiException("Gemini 호출에 실패했습니다: " + e.getStatusCode().value(), e);
        } catch (RestClientException e) {
            throw new AiException("Gemini 호출에 실패했습니다", e);
        }
    }

    private String extractText(GenerateResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            throw new AiException("Gemini 응답이 비어 있습니다");
        }
        Content content = response.candidates().getFirst().content();
        if (content == null || content.parts() == null || content.parts().isEmpty()) {
            throw new AiException("Gemini 응답에 내용이 없습니다");
        }
        String text = content.parts().getFirst().text();
        if (text == null || text.isBlank()) {
            throw new AiException("Gemini 응답 텍스트가 비어 있습니다");
        }
        return text;
    }

    private String stripCodeFence(String text) {
        String trimmed = text.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }
        int firstLineEnd = trimmed.indexOf('\n');
        int lastFence = trimmed.lastIndexOf("```");
        if (firstLineEnd < 0 || lastFence <= firstLineEnd) {
            return trimmed;
        }
        return trimmed.substring(firstLineEnd + 1, lastFence).trim();
    }

    record GenerateResponse(List<Candidate> candidates) {
    }

    record Candidate(Content content) {
    }

    record Content(List<Part> parts) {
    }

    record Part(String text) {
    }
}
