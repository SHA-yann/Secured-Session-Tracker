package com.tm.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a refresh token.
 * Used for issuing and validating refresh tokens for JWT authentication.
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "Refresh_tokens", indexes = {
        @Index(name = "idx_rt_token", columnList = "token", unique = true),
        @Index(name = "idx_rt_user", columnList = "user_id")
})
public class RefreshToken {

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /** Unique token string (UUID), immutable */
    @Column(nullable = false, unique = true, updatable = false)
    private String token;

    /** The user this refresh token belongs to */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Expiration timestamp */
    @Column(nullable = false)
    private Instant expiresAt;

    /** Flag indicating whether the token has been revoked */
    @Column(nullable = false)
    private boolean revoked = false;

    /**
     * Constructor generating a new refresh token for a user.
     *
     * @param user       the user associated with the token
     * @param expiresAt  the expiration timestamp
     */
    public RefreshToken(User user, Instant expiresAt) {
        this.token = UUID.randomUUID().toString();
        this.user = user;
        this.expiresAt = expiresAt;
    }
}
