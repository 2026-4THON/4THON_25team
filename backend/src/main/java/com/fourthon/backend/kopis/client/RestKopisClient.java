package com.fourthon.backend.kopis.client;

import com.fourthon.backend.common.client.RequestThrottle;
import com.fourthon.backend.kopis.config.KopisProperties;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RestKopisClient implements KopisClient {

    private static final DateTimeFormatter PARAM_DATE = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter PERIOD_DATE = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter UPDATED_AT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String SEOUL_CODE = "11";
    private static final String YES = "Y";
    private static final String ROW = "db";

    private final RestClient kopisRestClient;
    private final RequestThrottle kopisThrottle;
    private final KopisProperties kopisProperties;

    @Override
    public List<KopisResponse.Performance> fetchPerformances(
            LocalDate from,
            LocalDate to,
            LocalDate updatedAfter,
            int page,
            int rows) {

        Map<String, String> params = new LinkedHashMap<>();
        params.put("stdate", from.format(PARAM_DATE));
        params.put("eddate", to.format(PARAM_DATE));
        params.put("cpage", String.valueOf(page));
        params.put("rows", String.valueOf(rows));
        params.put("signgucode", SEOUL_CODE);
        params.put("signgucodesub", kopisProperties.sync().districtCode());
        if (updatedAfter != null) {
            params.put("afterdate", updatedAfter.format(PARAM_DATE));
        }

        Document document = request("/pblprfr", params);
        return KopisXmlReader.elements(document, ROW).stream()
                .map(row -> new KopisResponse.Performance(
                        KopisXmlReader.text(row, "mt20id"),
                        KopisXmlReader.text(row, "prfnm")
                ))
                .filter(performance -> !performance.kopisId().isEmpty())
                .toList();
    }

    @Override
    public Optional<KopisResponse.PerformanceDetail> fetchPerformanceDetail(String kopisId) {
        Document document = request("/pblprfr/" + kopisId, Map.of());
        return KopisXmlReader.elements(document, ROW).stream()
                .findFirst()
                .map(this::toPerformanceDetail);
    }

    @Override
    public Optional<KopisResponse.Facility> fetchFacility(String kopisVenueId) {
        Document document = request("/prfplc/" + kopisVenueId, Map.of());
        return KopisXmlReader.elements(document, ROW).stream()
                .findFirst()
                .map(row -> new KopisResponse.Facility(
                        KopisXmlReader.text(row, "mt10id"),
                        KopisXmlReader.text(row, "fcltynm"),
                        KopisXmlReader.text(row, "adres"),
                        parseCoordinate(KopisXmlReader.text(row, "la")),
                        parseCoordinate(KopisXmlReader.text(row, "lo"))
                ));
    }

    @Override
    public List<KopisResponse.BoxOffice> fetchBoxOffice(LocalDate date) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("stdate", date.format(PARAM_DATE));
        params.put("eddate", date.format(PARAM_DATE));
        params.put("area", kopisProperties.sync().boxOfficeArea());

        Document document = request("/boxoffice", params);
        return KopisXmlReader.elements(document, "boxof").stream()
                .map(row -> new KopisResponse.BoxOffice(
                        parseRank(KopisXmlReader.text(row, "rnum")),
                        KopisXmlReader.text(row, "mt20id")
                ))
                .filter(boxOffice -> boxOffice.rank() > 0 && !boxOffice.kopisId().isEmpty())
                .toList();
    }

    private Document request(String path, Map<String, String> params) {
        String apiKey = kopisProperties.apiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new KopisException("KOPIS_API_KEY가 설정되지 않았습니다");
        }
        kopisThrottle.acquire();
        try {
            byte[] body = kopisRestClient.get()
                    .uri(uriBuilder -> buildUri(uriBuilder, path, apiKey, params))
                    .retrieve()
                    .body(byte[].class);
            return KopisXmlReader.parse(body);
        } catch (RestClientResponseException e) {
            throw new KopisBlockedException(
                    "KOPIS가 요청을 거부했습니다: " + path + " (" + e.getStatusCode().value() + ")", e);
        } catch (RestClientException e) {
            throw new KopisException("KOPIS 호출에 실패했습니다: " + path, e);
        }
    }

    private URI buildUri(
            UriBuilder uriBuilder,
            String path,
            String apiKey,
            Map<String, String> params) {

        uriBuilder.path(path).queryParam("service", apiKey);
        params.forEach(uriBuilder::queryParam);
        return uriBuilder.build();
    }

    private KopisResponse.PerformanceDetail toPerformanceDetail(Element row) {
        return new KopisResponse.PerformanceDetail(
                KopisXmlReader.text(row, "mt20id"),
                KopisXmlReader.text(row, "mt10id"),
                KopisXmlReader.text(row, "prfnm"),
                parseDate(KopisXmlReader.text(row, "prfpdfrom")),
                parseDate(KopisXmlReader.text(row, "prfpdto")),
                KopisXmlReader.text(row, "fcltynm"),
                KopisXmlReader.text(row, "prfcast"),
                KopisXmlReader.text(row, "prfruntime"),
                KopisXmlReader.text(row, "prfage"),
                KopisXmlReader.text(row, "pcseguidance"),
                KopisXmlReader.text(row, "poster"),
                KopisXmlReader.text(row, "sty"),
                KopisXmlReader.text(row, "genrenm"),
                YES.equals(KopisXmlReader.text(row, "openrun")),
                YES.equals(KopisXmlReader.text(row, "daehakro")),
                parseDateTime(KopisXmlReader.text(row, "updatedate")),
                KopisXmlReader.text(row, "dtguidance"),
                introImageUrls(row),
                bookingLinks(row)
        );
    }

    private List<String> introImageUrls(Element row) {
        return KopisXmlReader.child(row, "styurls")
                .map(urls -> KopisXmlReader.children(urls, "styurl").stream()
                        .map(Element::getTextContent)
                        .map(String::trim)
                        .filter(url -> !url.isEmpty())
                        .toList())
                .orElse(List.of());
    }

    private List<KopisResponse.BookingLink> bookingLinks(Element row) {
        return KopisXmlReader.child(row, "relates")
                .map(relates -> KopisXmlReader.children(relates, "relate").stream()
                        .map(relate -> new KopisResponse.BookingLink(
                                KopisXmlReader.text(relate, "relatenm"),
                                KopisXmlReader.text(relate, "relateurl")
                        ))
                        .filter(link -> !link.url().isEmpty())
                        .toList())
                .orElse(List.of());
    }

    private LocalDate parseDate(String text) {
        try {
            return text.isEmpty() ? null : LocalDate.parse(text, PERIOD_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(String text) {
        try {
            return text.isEmpty() ? null : LocalDateTime.parse(text, UPDATED_AT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Double parseCoordinate(String text) {
        try {
            return text.isEmpty() ? null : Double.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseRank(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
