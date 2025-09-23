package com.um.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Entity representing an application user.
 * Includes fields for authentication, role, timestamps, and refresh tokens.
 */
@Entity
@NoArgsConstructor
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true)
})
@Getter
@Setter
public class User {

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;

    /** Username, required, max length 20 */
    @NotBlank
    @Column(nullable = false, length = 20)
    private String username;

    /** Email, required, unique, validated format, max length 50 */
    @Email
    @Column(nullable = false, unique = true, length = 50)
    private String email;

    /** Password, required */
    @NotBlank
    @Column(nullable = false)
    private String password;

    /** Role of the user (e.g., USER, ADMIN), stored as string */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    /** Timestamp when the user was created, immutable */
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /** Timestamp of last update */
    @Column(nullable = false)
    private Instant updatedAt;

    /** List of refresh tokens associated with the user */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    /**
     * Automatically sets creation and update timestamps before persisting.
     */
    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    /**
     * Updates the updatedAt timestamp before updating.
     */
    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Constructor for creating a new user with mandatory fields.
     *
     * @param username username
     * @param email    user email
     * @param password user password
     */
    public User(@NotBlank String username, @Email String email, @NotBlank String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
