package com.fourthon.backend.show.client;

import com.fourthon.backend.ai.client.ImageInput;
import com.fourthon.backend.common.client.RequestThrottle;
import com.fourthon.backend.show.config.ShowAnalysisProperties;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RestShowImageClient implements ShowImageClient {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(60);

    private final RestClient restClient;
    private final RequestThrottle showImageThrottle;
    private final ShowAnalysisProperties showAnalysisProperties;

    public RestShowImageClient(RequestThrottle showImageThrottle, ShowAnalysisProperties showAnalysisProperties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
        this.showImageThrottle = showImageThrottle;
        this.showAnalysisProperties = showAnalysisProperties;
    }

    @Override
    public List<ImageInput> fetch(List<String> imageUrls) {
        List<ImageInput> slices = new ArrayList<>();
        for (String imageUrl : imageUrls.stream().limit(showAnalysisProperties.maxIntroImages()).toList()) {
            int remaining = showAnalysisProperties.maxImageSlices() - slices.size();
            if (remaining <= 0) {
                break;
            }
            slices.addAll(fetchOne(imageUrl, remaining));
        }
        return slices;
    }

    private List<ImageInput> fetchOne(String imageUrl, int maxSlices) {
        showImageThrottle.acquire();
        try {
            byte[] body = restClient.get()
                    .uri(URI.create(imageUrl.strip()))
                    .retrieve()
                    .body(byte[].class);
            if (body == null || body.length == 0 || body.length > showAnalysisProperties.maxDownloadBytes()) {
                return List.of();
            }
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(body));
            if (image == null) {
                return List.of();
            }
            return ImageSlicer.slice(
                    image,
                    showAnalysisProperties.imageMaxWidth(),
                    showAnalysisProperties.imageSliceHeight(),
                    maxSlices
            );
        } catch (RestClientException | IllegalArgumentException | IOException | UncheckedIOException e) {
            log.warn("소개 이미지를 받지 못함: {}", imageUrl, e);
            return List.of();
        }
    }
}
