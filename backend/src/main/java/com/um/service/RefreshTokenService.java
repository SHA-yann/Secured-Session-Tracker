package com.um.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.um.model.RefreshToken;
import com.um.model.User;
import com.um.repository.RefreshTokenRepository;

/**
 * Service managing the lifecycle of refresh tokens.
 * Handles issuing, verifying, rotating, and revoking tokens.
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${refresh.expiration-days}")
    private long refreshDays;

    /**
     * Constructor injecting the refresh token repository.
     *
     * @param refreshTokenRepository repository for refresh tokens
     */
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Issues a new refresh token for a user.
     *
     * @param user the user to issue the token for
     * @return the saved RefreshToken
     */
    public RefreshToken issue(User user) {
        Instant expiration = Instant.now().plus(refreshDays, ChronoUnit.DAYS);
        RefreshToken rt = new RefreshToken(user, expiration);
        return refreshTokenRepository.save(rt);
    }

    /**
     * Verifies a refresh token for validity.
     *
     * @param token the refresh token string
     * @return the valid RefreshToken entity
     * @throws IllegalArgumentException if the token is not found
     * @throws IllegalStateException if the token is expired or revoked
     */
    public RefreshToken verify(String token) {
        RefreshToken rt = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (rt.isRevoked() || Instant.now().isAfter(rt.getExpiresAt())) {
            throw new IllegalStateException("Refresh token expired or revoked");
        }
        return rt;
    }

    /**
     * Rotates a refresh token: revokes the old one and issues a new one.
     *
     * @param oldToken the existing refresh token to rotate
     * @return the newly issued RefreshToken
     */
    public RefreshToken rotate(RefreshToken oldToken) {
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);
        return issue(oldToken.getUser());
    }

    /**
     * Revokes all refresh tokens for a given user.
     *
     * @param userId ID of the user whose tokens should be revoked
     */
    public void revokeUserTokens(long userId) {
        refreshTokenRepository.findAll().stream()
                .filter(rt -> rt.getUser().getId() == userId)
                .forEach(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
    }
}
