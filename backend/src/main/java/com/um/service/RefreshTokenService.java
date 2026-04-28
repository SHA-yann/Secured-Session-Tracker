package com.um.service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.um.Exceptions.ResourceNotFoundException;
import com.um.configuration.JwtProvider;
import com.um.model.RefreshToken;
import com.um.model.User;
import com.um.repository.RefreshTokenRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Service managing the lifecycle of refresh tokens.
 * Handles issuing, verifying, rotating, and revoking tokens.
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private JwtProvider jwtProvider;
    private ReactiveStringRedisTemplate rsrt;

    @Value("${refresh.expiration-days}")
    private long refreshDays;
    

    /**
     * Constructor injecting the refresh token repository.
     *
     * @param refreshTokenRepository repository for refresh tokens
     */
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
    							JwtProvider jwtProvider,
    							ReactiveStringRedisTemplate rsrt) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProvider = jwtProvider;
        this.rsrt = rsrt;
    }

    /**
     * Issues a new refresh token for a user.
     *
     * @param user the user to issue the token for
     * @return the saved RefreshToken
     */
    @Transactional
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
     * @throws ResourceNotFoundException if the token is not found
     * @throws IllegalStateException if the token is expired or revoked
     */
    @Transactional(readOnly = true)
    public RefreshToken verify(String token) {
        RefreshToken rt = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

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
    @Transactional
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
    @Transactional
    public Mono<Void> revokeUserTokens(long userId) {
    	
        return Mono.fromCallable(() -> refreshTokenRepository.findByUserId(userId))
        			.subscribeOn(Schedulers.boundedElastic())
	                .flatMap(tokens -> {
	                	if(tokens.isEmpty())
	                		return Mono.empty();
	                	tokens.forEach(rt -> rt.setRevoked(true));
	                	return Mono.fromCallable(() -> refreshTokenRepository.saveAll(tokens))
	                				.subscribeOn(Schedulers.boundedElastic())
	                				.flatMapMany(Flux::fromIterable)
	                				.flatMap(rt ->{
					                	long ttl = jwtProvider.extractExpiration(rt.getToken()).getTime() - System.currentTimeMillis();
					                    if(ttl >0)
					                    	return rsrt.opsForValue().set("Blacklist:"+rt.getToken(),"true",Duration.ofMillis(ttl));
					                    
					                    return Mono.empty();
	                				})
	                				.then();
	                });
        			
    }
}
