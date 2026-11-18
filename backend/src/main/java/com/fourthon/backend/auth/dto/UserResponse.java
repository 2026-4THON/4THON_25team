
package com.fourthon.backend.auth.dto;

import com.fourthon.backend.user.entity.User;

public record UserResponse(
        Long id,
        String name,
        String email,
        String profileImage
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProfileImage()
        );
    }
}
