
package com.fourthon.backend.auth.config;

import com.fourthon.backend.auth.service.CustomOidcUserService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOidcUserService customOidcUserService;
    private final String frontendUrl;

    public SecurityConfig(
            CustomOidcUserService customOidcUserService,
            @Value("${app.frontend-url}") String frontendUrl) {

        this.customOidcUserService = customOidcUserService;
        this.frontendUrl = frontendUrl;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        // CSRF 토큰 쿠키 설정
        CookieCsrfTokenRepository repo =
                CookieCsrfTokenRepository.withHttpOnlyFalse();

        // 크로스 사이트 환경에서 쿠키 전송 허용
        repo.setCookieCustomizer(cookie -> cookie
                .sameSite("None")
                .secure(true)
        );

        http
                // 프론트엔드 요청 허용
                .cors(cors -> cors.configurationSource(
                        corsConfigurationSource()
                ))

                // CSRF 보호 유지 + 원본 토큰 사용
                .csrf(csrf -> csrf
                        .csrfTokenRepository(repo)
                        .csrfTokenRequestHandler(
                                new CsrfTokenRequestAttributeHandler()
                        )
                )

                // API 접근 권한
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/error",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/auth/me",
                                "/auth/csrf",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/index.html",
                                "/start",
                                "/assets/**",
                                "/favicon.ico",
                                "/favicon.svg"
                        ).permitAll()

                        .anyRequest().authenticated()
                )

                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                PathPatternRequestMatcher.withDefaults().matcher("/api/**")
                        )
                )

                // 구글 OAuth2 로그인
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService)
                        )
                        .defaultSuccessUrl(frontendUrl, true)
                )

                // 로그아웃
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler(
                                (request, response, authentication) -> {
                                    response.setStatus(
                                            HttpStatus.NO_CONTENT.value()
                                    );
                                }
                        )
                );

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource
    corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // 허용할 프론트엔드 주소
        config.setAllowedOrigins(
                List.of(frontendUrl)
        );

        // 허용할 HTTP 메서드
        config.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        // 허용할 요청 헤더
        config.setAllowedHeaders(
                List.of(
                        "Content-Type",
                        "Authorization",
                        "X-CSRF-TOKEN",
                        "X-XSRF-TOKEN"
                )
        );

        // 쿠키를 포함한 요청 허용
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
