package com.fourthon.backend.internal.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RequiredArgsConstructor
public class AdminKeyFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Admin-Key";
    private static final String UNAUTHORIZED_BODY = "{\"message\":\"관리자 키가 올바르지 않습니다\"}";

    private final AdminProperties adminProperties;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (matches(request.getHeader(HEADER))) {
            filterChain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(UNAUTHORIZED_BODY);
    }

    private boolean matches(String providedKey) {
        String expectedKey = adminProperties.apiKey();
        if (expectedKey == null || expectedKey.isBlank() || providedKey == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expectedKey.getBytes(StandardCharsets.UTF_8),
                providedKey.getBytes(StandardCharsets.UTF_8)
        );
    }
}
