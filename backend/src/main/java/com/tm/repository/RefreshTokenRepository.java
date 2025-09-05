package com.tm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tm.model.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	
	Optional<RefreshToken> findByToken(String token);
	
	Long deleteByUser(Long userId);
}
