package com.um.service;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.um.Exceptions.ResourceNotFoundException;
import com.um.configuration.CookieProvider;
import com.um.configuration.JwtProvider;
import com.um.dto.AuthRequest;
import com.um.model.RefreshToken;
import com.um.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Service handling user authentication, JWT issuance, refresh token rotation,
 * and logout operations.
 */
@Service
@Slf4j
public class AuthService {

    private final ReactiveAuthenticationManager authenticationManager;
    private final UserRepository userRepo;
    private final MyUserDetailsService myUserDetailsService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    
    @Value("${security.cookie.domain}")
    private String cookieDomain;

    @Value("${security.cookie.secure}")
    private boolean cookieSecure;

    private static final String REFRESH_COOKIE = "refresh_token";

    public AuthService(ReactiveAuthenticationManager authenticationManager,UserRepository userRepo,
                       JwtProvider jwtProvider,
                       MyUserDetailsService myUserDetailsService,
                       RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepo = userRepo;
        this.jwtProvider = jwtProvider;
        this.myUserDetailsService = myUserDetailsService;
        this.refreshTokenService = refreshTokenService;
    }
    
    
    @Transactional
    public Mono<AuthResult> login(AuthRequest request) {
    	
    	return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()))
    								.flatMap(auth ->{
    									UserDetails userDetails = (UserDetails) auth.getPrincipal();
    									String token = jwtProvider.generateToken(userDetails);
    									return Mono.fromCallable(() -> 
    											userRepo.findByUsername(auth.getName()))
       											.subscribeOn(Schedulers.boundedElastic())
    											.flatMap(oP -> Mono.justOrEmpty(oP))
    											//.log("DEBUG_LOGIN")
    											.flatMap( user -> {
    												return Mono.fromCallable(() -> refreshTokenService.issue(user))
    														.subscribeOn(Schedulers.boundedElastic())
    														.map( rt -> {
			    												int maxAge = (int) (rt.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond());
			    												ResponseCookie refreshCookie = CookieProvider.createCookie(REFRESH_COOKIE, rt.getToken(), cookieDomain, cookieSecure, maxAge);
			    										        return new AuthResult(token, refreshCookie);
    														});
    											});
    											
    								});
    	
    }

    
    @Transactional(readOnly = true)
    public Mono<AuthResult> refresh(String rToken) {
    	
        if (rToken == null || rToken.isBlank()) 
        	return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,"Missing Refresh token"));
        
        return Mono.fromCallable(()->{
        	try {
	        	RefreshToken currentRt = refreshTokenService.verify(rToken);
	        	return refreshTokenService.rotate(currentRt);
        	}catch(ResourceNotFoundException | IllegalStateException e) {
        		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,e.getMessage());
        	}
    			}).subscribeOn(Schedulers.boundedElastic())
        		  //.log("DEBUG_REFRESH")
        		  .flatMap(nextRt -> {
        			  String username = nextRt.getUser().getUsername();
        			  return myUserDetailsService.findByUsername(username)
        					  .map(uD -> {
        						String newAccess = jwtProvider.generateToken(uD);
		        			    int maxAge = (int) (nextRt.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond());
						        ResponseCookie newRefreshCookie = CookieProvider.createCookie(REFRESH_COOKIE, nextRt.getToken(), cookieDomain, cookieSecure, maxAge);
						        return new AuthResult(newAccess, newRefreshCookie);
        					  	});
        		  });
        	
    }

    
    @Transactional
    public Mono<Void> logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            log.debug("Logout ignoré : refresh token vide ou nul.");
            return Mono.empty();
        }

        return Mono.fromCallable(() -> refreshTokenService.verify(refreshToken))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(currentRt -> 
                    Mono.fromRunnable(() -> refreshTokenService.revokeUserTokens(currentRt.getUser().getId()))
                        .subscribeOn(Schedulers.boundedElastic())
                )
                .doOnSuccess(v -> log.info("Déconnexion réussie et tokens révoqués en base."))
                .onErrorResume(e -> {
                    log.warn("Tentative de déconnexion avec un token invalide, expiré ou déjà révoqué : {}", e.getMessage());
                    return Mono.empty(); 
                })
                .then(); 
    }
}


