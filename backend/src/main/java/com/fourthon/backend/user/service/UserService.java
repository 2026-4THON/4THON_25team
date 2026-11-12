
package com.fourthon.backend.user.service;

import com.fourthon.backend.user.entity.User;
import com.fourthon.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User findOrCreateUser(
            String googleId,
            String email,
            String name,
            String profileImage) {

        return userRepository.findByGoogleId(googleId)
                .orElseGet(() -> {
                    User newUser = new User(
                            googleId,
                            email,
                            name,
                            profileImage
                    );

                    return userRepository.save(newUser);
                });
    }
}
