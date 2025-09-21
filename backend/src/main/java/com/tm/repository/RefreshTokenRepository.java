package com.tm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tm.model.RefreshToken;

/**
 * Repository interface for RefreshToken entities.
 * Provides CRUD operations and token lookup functionality.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Finds a refresh token by its string value.
     *
     * @param token the refresh token string
     * @return Optional containing the RefreshToken entity if found
     */
    Optional<RefreshToken> findByToken(String token);
}
