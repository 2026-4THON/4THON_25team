package com.fourthon.backend.show.client;

import com.fourthon.backend.show.config.YoutubeProperties;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.HtmlUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
public class RestYoutubeClient implements YoutubeClient {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);
    private static final String QUOTA_EXCEEDED_REASON = "quotaExceeded";
    private static final String REGION_CODE = "KR";
    private static final String RELEVANCE_LANGUAGE = "ko";

    private final RestClient restClient;
    private final YoutubeProperties youtubeProperties;

    public RestYoutubeClient(YoutubeProperties youtubeProperties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        this.restClient = RestClient.builder()
                .baseUrl(youtubeProperties.baseUrl())
                .requestFactory(requestFactory)
                .build();
        this.youtubeProperties = youtubeProperties;
    }

    @Override
    public boolean isConfigured() {
        String apiKey = youtubeProperties.apiKey();
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public List<VideoCandidate> search(String query, int maxResults) {
        if (!isConfigured()) {
            throw new YoutubeException("YOUTUBE_API_KEY가 설정되지 않았습니다");
        }
        SearchResponse response = request(query, maxResults);
        if (response == null || response.items() == null) {
            return List.of();
        }
        return response.items().stream()
                .filter(item -> item.id() != null && item.id().videoId() != null && item.snippet() != null)
                .map(item -> new VideoCandidate(
                        item.id().videoId(),
                        unescape(item.snippet().title()),
                        unescape(item.snippet().channelTitle()),
                        toDate(item.snippet().publishedAt())
                ))
                .toList();
    }

    private SearchResponse request(String query, int maxResults) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/search")
                            .queryParam("part", "snippet")
                            .queryParam("q", "{query}")
                            .queryParam("type", "video")
                            .queryParam("videoEmbeddable", "true")
                            .queryParam("maxResults", maxResults)
                            .queryParam("regionCode", REGION_CODE)
                            .queryParam("relevanceLanguage", RELEVANCE_LANGUAGE)
                            .queryParam("key", "{key}")
                            .build(query, youtubeProperties.apiKey()))
                    .retrieve()
                    .body(SearchResponse.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().isSameCodeAs(HttpStatus.FORBIDDEN)
                    && e.getResponseBodyAsString().contains(QUOTA_EXCEEDED_REASON)) {
                throw new YoutubeQuotaExceededException("YouTube 검색 할당량을 모두 썼습니다", e);
            }
            throw new YoutubeException("YouTube 검색에 실패했습니다: " + e.getStatusCode().value(), e);
        } catch (RestClientException e) {
            throw new YoutubeException("YouTube 검색에 실패했습니다", e);
        }
    }

    private String unescape(String text) {
        if (text == null) {
            return "";
        }
        return HtmlUtils.htmlUnescape(text);
    }

    private LocalDate toDate(String publishedAt) {
        if (publishedAt == null || publishedAt.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(publishedAt).toLocalDate();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    record SearchResponse(List<Item> items) {
    }

    record Item(ItemId id, Snippet snippet) {
    }

    record ItemId(String videoId) {
    }

    record Snippet(String title, String channelTitle, String publishedAt) {
    }
}
