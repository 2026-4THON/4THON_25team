
package com.fourthon.backend.auth.service;

import com.fourthon.backend.user.service.UserService;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class CustomOidcUserService extends OidcUserService {

    private final UserService userService;

    public CustomOidcUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {

        // Google 사용자 정보 조회
        OidcUser oidcUser = super.loadUser(userRequest);

        String googleId = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String profileImage =
                oidcUser.getClaimAsString("picture");

        if (googleId == null || email == null) {
            throw new IllegalStateException(
                    "필수 구글 사용자 정보가 없습니다."
            );
        }

        // 기존 회원 조회 또는 신규 회원가입
        userService.findOrCreateUser(
                googleId,
                email,
                name,
                profileImage
        );

        return oidcUser;
    }
}
