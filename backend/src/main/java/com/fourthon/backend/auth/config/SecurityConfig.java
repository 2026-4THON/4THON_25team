
package com.fourthon.backend.auth.config;

import com.fourthon.backend.auth.service.CustomOidcUserService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

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

        http
                // 프론트엔드 요청 허용
                .cors(cors -> cors.configurationSource(
                        corsConfigurationSource()
                ))

                // CSRF 보호 유지 + SPA 쿠키 기반 토큰 처리
                .csrf(csrf -> csrf.spa())

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
                                "/v3/api-docs/**"
                        ).permitAll()

                        .anyRequest().authenticated()
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

        config.setAllowedOrigins(
                List.of(frontendUrl)
        );

        config.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE")
        );

        config.setAllowedHeaders(
                List.of(
                        "Content-Type",
                        "X-CSRF-TOKEN",
                        "X-XSRF-TOKEN"
                )
        );

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
