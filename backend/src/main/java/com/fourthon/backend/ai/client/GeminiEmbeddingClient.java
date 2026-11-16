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
public class GeminiEmbeddingClient implements EmbeddingClient {

    private static final String API_KEY_HEADER = "x-goog-api-key";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);

    private final RestClient restClient;
    private final GeminiProperties geminiProperties;

    public GeminiEmbeddingClient(GeminiProperties geminiProperties) {
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
    public String model() {
        return geminiProperties.embeddingModel();
    }

    @Override
    public float[] embed(String text) {
        if (!isConfigured()) {
            throw new AiException("GEMINI_API_KEY가 설정되지 않았습니다");
        }
        Map<String, Object> body = Map.of(
                "content", Map.of("parts", List.of(Map.of("text", text))),
                "outputDimensionality", geminiProperties.embeddingDimension()
        );
        try {
            EmbedResponse response = restClient.post()
                    .uri("/models/{model}:embedContent", geminiProperties.embeddingModel())
                    .header(API_KEY_HEADER, geminiProperties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(EmbedResponse.class);
            return normalize(extractValues(response));
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS)) {
                throw new AiRateLimitException("Gemini 요청 한도를 넘었습니다", e);
            }
            throw new AiException("Gemini 임베딩 호출에 실패했습니다: " + e.getStatusCode().value(), e);
        } catch (RestClientException e) {
            throw new AiException("Gemini 임베딩 호출에 실패했습니다", e);
        }
    }

    private List<Float> extractValues(EmbedResponse response) {
        if (response == null || response.embedding() == null
                || response.embedding().values() == null || response.embedding().values().isEmpty()) {
            throw new AiException("Gemini 임베딩 응답이 비어 있습니다");
        }
        return response.embedding().values();
    }

    private float[] normalize(List<Float> values) {
        double sum = 0.0;
        for (Float value : values) {
            sum += value * value;
        }
        double norm = Math.sqrt(sum);
        if (norm == 0.0) {
            throw new AiException("Gemini 임베딩 벡터가 0입니다");
        }
        float[] vector = new float[values.size()];
        for (int i = 0; i < vector.length; i++) {
            vector[i] = (float) (values.get(i) / norm);
        }
        return vector;
    }

    record EmbedResponse(Embedding embedding) {
    }

    record Embedding(List<Float> values) {
    }
}
