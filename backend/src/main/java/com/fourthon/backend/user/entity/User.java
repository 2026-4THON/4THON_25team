
package com.fourthon.backend.user.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_users_google_id",
                columnNames = "google_id"
        )
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "google_id",
            nullable = false,
            length = 255)
    private String googleId;

    @Column(nullable = false)
    private String email;

    @Column
    private String name;

    @Column(name = "profile_image",
            length = 1000)
    private String profileImage;

    protected User() {
    }

    public User(
            String googleId,
            String email,
            String name,
            String profileImage) {

        this.googleId = googleId;
        this.email = email;
        this.name = name;
        this.profileImage = profileImage;
    }

    public Long getId() {
        return id;
    }

    public String getGoogleId() {
        return googleId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getProfileImage() {
        return profileImage;
    }
}
