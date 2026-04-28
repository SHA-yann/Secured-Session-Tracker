package com.um.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.um.model.RefreshToken;

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
	@EntityGraph(attributePaths = {"user"})
    Optional<RefreshToken> findByToken(String token);
	
	List<RefreshToken> findByUserId(Long userId);
}
