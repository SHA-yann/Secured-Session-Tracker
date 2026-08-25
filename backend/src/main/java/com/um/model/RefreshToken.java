package com.um.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a refresh token used for JWT authentication.
 * <p>
 * Each refresh token is associated with a user and has an expiration time.
 * Tokens can be revoked to invalidate them before their expiration.
 * </p>
 * <p>
 * This entity supports database indexing on the token value for fast lookup
 * and on the user for query optimization.
 * </p>
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(
    name = "Refresh_tokens",
    indexes = {
        @Index(name = "idx_rt_token", columnList = "rtToken", unique = true),
        @Index(name = "idx_rt_user", columnList = "user_id")
    }
)
public class RefreshToken {

    /** Primary key of the refresh token record */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /** Unique token string (UUID), immutable once created */
    @Column(nullable = false, unique = true, updatable = false)
    private String rtToken;

    /** The user this refresh token is associated with */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Expiration timestamp of the refresh token */
    @Column(nullable = false)
    private Instant expiresAt;

    /** Flag indicating whether the token has been revoked */
    @Column(nullable = false)
    private boolean revoked = false;

    /**
     * Constructs a new refresh token for a user with a specific expiration date.
     * The token string is automatically generated as a random UUID.
     *
     * @param user      the user associated with this refresh token
     * @param expiresAt the timestamp at which the token expires
     */
    public RefreshToken(User user, Instant expiresAt) {
        this.rtToken = UUID.randomUUID().toString();
        this.user = user;
        this.expiresAt = expiresAt;
    }
}
