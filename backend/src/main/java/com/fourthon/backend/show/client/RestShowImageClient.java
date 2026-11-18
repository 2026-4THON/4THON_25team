package com.fourthon.backend.show.client;

import com.fourthon.backend.ai.client.ImageInput;
import com.fourthon.backend.common.client.RequestThrottle;
import com.fourthon.backend.show.config.ShowAnalysisProperties;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Component
public class RestShowImageClient implements ShowImageClient {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);
    private static final String JPEG = "image/jpeg";
    private static final String PNG = "image/png";
    private static final String WEBP = "image/webp";
    private static final String GIF = "image/gif";
    private static final String PNG_FORMAT = "png";

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
        List<ImageInput> images = new ArrayList<>();
        for (String imageUrl : imageUrls) {
            if (images.size() >= showAnalysisProperties.maxIntroImages()) {
                break;
            }
            fetchOne(imageUrl).ifPresent(images::add);
        }
        return images;
    }

    private Optional<ImageInput> fetchOne(String imageUrl) {
        showImageThrottle.acquire();
        try {
            ResponseEntity<byte[]> response = restClient.get()
                    .uri(URI.create(imageUrl.strip()))
                    .retrieve()
                    .toEntity(byte[].class);
            byte[] body = response.getBody();
            if (body == null || body.length == 0) {
                return Optional.empty();
            }
            return toSupported(mimeType(response.getHeaders().getContentType(), imageUrl), body)
                    .filter(image -> image.data().length <= showAnalysisProperties.maxImageBytes());
        } catch (RestClientException | IllegalArgumentException | IOException e) {
            log.warn("소개 이미지를 받지 못함: {}", imageUrl, e);
            return Optional.empty();
        }
    }

    private Optional<ImageInput> toSupported(String mimeType, byte[] body) throws IOException {
        if (JPEG.equals(mimeType) || PNG.equals(mimeType) || WEBP.equals(mimeType)) {
            return Optional.of(new ImageInput(mimeType, body));
        }
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(body));
        if (image == null) {
            return Optional.empty();
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, PNG_FORMAT, output);
        return Optional.of(new ImageInput(PNG, output.toByteArray()));
    }

    private String mimeType(MediaType contentType, String imageUrl) {
        if (contentType != null && "image".equals(contentType.getType())) {
            String subtype = contentType.getSubtype().toLowerCase(Locale.ROOT);
            return "jpg".equals(subtype) ? JPEG : "image/" + subtype;
        }
        String lower = imageUrl.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return JPEG;
        }
        if (lower.endsWith(".png")) {
            return PNG;
        }
        if (lower.endsWith(".webp")) {
            return WEBP;
        }
        return GIF;
    }
}
