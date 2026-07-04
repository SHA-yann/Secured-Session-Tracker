package com.um.service;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.um.Exceptions.ResourceNotFoundException;
import com.um.configuration.CookieProvider;
import com.um.configuration.JwtProvider;
import com.um.dto.AuthRequest;
import com.um.model.RefreshToken;
import com.um.repository.RefreshTokenRepository;
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
    private final NotificationService notificationService;
    private final RefreshTokenRepository refreshTokenRepository;
    
    @Value("${security.cookie.domain}")
    private String cookieDomain;

    @Value("${security.cookie.secure}")
    private boolean cookieSecure;

    private static final String REFRESH_COOKIE = "refresh_token";

    public AuthService(ReactiveAuthenticationManager authenticationManager,UserRepository userRepo,
                       JwtProvider jwtProvider,
                       MyUserDetailsService myUserDetailsService,
                       RefreshTokenService refreshTokenService,
                       NotificationService notificationService, RefreshTokenRepository refreshTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepo = userRepo;
        this.jwtProvider = jwtProvider;
        this.myUserDetailsService = myUserDetailsService;
        this.refreshTokenService = refreshTokenService;
        this.notificationService = notificationService;
		this.refreshTokenRepository = refreshTokenRepository;
    }
    
    
    @Transactional
    public Mono<AuthResult> login(AuthRequest request) {
    	
    	return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()))
					//.log("DEBUG_LOGIN")
					.flatMap(auth ->{
					UserDetails userDetails = (UserDetails) auth.getPrincipal();
					String token = jwtProvider.generateToken(userDetails);
					return Mono.fromCallable(() -> 
							userRepo.findByUsername(auth.getName()))
							.subscribeOn(Schedulers.boundedElastic())
							.flatMap(oP -> Mono.justOrEmpty(oP))
							.flatMap( user -> {
								return Mono.fromCallable(() -> refreshTokenService.issue(user))
										.subscribeOn(Schedulers.boundedElastic())
										.map( rt -> {
											int maxAge = (int) (rt.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond());
											ResponseCookie refreshCookie = CookieProvider.createCookie(REFRESH_COOKIE, rt.getRtToken(), cookieDomain, maxAge);
									        log.info("Access token created");
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
						        ResponseCookie newRefreshCookie = CookieProvider.createCookie(REFRESH_COOKIE, nextRt.getRtToken(), cookieDomain, maxAge);
						        log.info("Access token refreshed");
						        return new AuthResult(newAccess, newRefreshCookie);
        					  	});
        		  });
        	
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public Mono<Void> logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            log.info("Logout ignored : refresh token invalid or nul.");
            return Mono.empty();
        }

        return ReactiveSecurityContextHolder.getContext()
        		//.log("DEBUG_LOGOUT")
        		.map(SecurityContext::getAuthentication)
        		.map(Authentication::getName)
        		.flatMap(currentUsername -> {
        			
        			return Mono.fromCallable(() -> {
        			
        				RefreshToken currentRt = refreshTokenService.verify(refreshToken);
        				String tokenOwner = currentRt.getUser().getUsername();    			        	
    			        	
			        	if(!tokenOwner.equals(currentUsername)) {
			        		log.info("Security Alert: User {} tried to logout using {}'s refresh token",currentUsername,tokenOwner);
			        		throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Token mismatch");
			        	}
    			        
			        	currentRt.setRevoked(true);
			        	refreshTokenRepository.save(currentRt);
						log.info("refresh token revoked for {}",tokenOwner);
						
						return currentRt.getUser();
        			})
					.subscribeOn(Schedulers.boundedElastic())
					.flatMap(user -> {
						return notificationService.removeOnlineUser(user.getId(), user.getUsername())
							   .doOnSuccess(v -> log.info("logout succesfull for {}",user.getUsername()));
					});
        		})
		        .onErrorResume(e -> {
		            log.warn("An error occured : {}", e.getMessage());
		            return Mono.empty();
		        })
		        .then();
         
    }
}

