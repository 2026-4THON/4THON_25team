
package com.fourthon.backend.user.repository;

import com.fourthon.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByGoogleId(String googleId);
}
