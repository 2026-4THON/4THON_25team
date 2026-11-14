
package com.fourthon.backend.auth.controller;

import com.fourthon.backend.auth.dto.UserResponse;
import com.fourthon.backend.auth.resolver.CurrentUser;
import com.fourthon.backend.user.entity.User;
import com.fourthon.backend.user.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(
        name = "인증 API",
        description = "구글 로그인 및 사용자 인증 관련 API"
)
@RestController
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 로그인한 사용자 정보 조회
    @Operation(
            summary = "내 정보 조회",
            description = "현재 로그인한 회원의 ID, 이름, 이메일, 프로필 이미지를 조회합니다."
    )
    @GetMapping("/auth/me")
    public UserResponse getMe(
            @Parameter(hidden = true)
            @CurrentUser Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "회원 정보를 찾을 수 없습니다."
                        )
                );

        return UserResponse.from(user);
    }

    // CSRF 토큰 조회
    @Operation(
            summary = "CSRF 토큰 조회",
            description = "로그아웃 등 상태 변경 요청에 필요한 CSRF 토큰을 조회합니다."
    )
    @GetMapping("/auth/csrf")
    public CsrfResponse getCsrf(
            HttpServletRequest request) {

        CsrfToken token = (CsrfToken)
                request.getAttribute(
                        CsrfToken.class.getName()
                );

        if (token == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "CSRF 토큰을 가져올 수 없습니다."
            );
        }

        return new CsrfResponse(
                token.getHeaderName(),
                token.getToken()
        );
    }

    public record CsrfResponse(
            String headerName,
            String token
    ) {}
}
