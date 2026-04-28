package com.um.configuration;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.um.model.Status;
import com.um.service.MyUserDetailsService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * JWT Authentication filter that intercepts each request once,
 * extracts the JWT token from the Authorization header,
 * validates it, and sets the security context accordingly.
 */
@Component
@Slf4j
public class JwtAuthFilter implements WebFilter {

    private final JwtProvider jwtProvider;
    private final MyUserDetailsService userDetailsService;

    /**
     * Constructor injecting dependencies.
     *
     * @param jwtProvider JWT utility for token parsing and validation
     * @param userDetailsService Service to load user details
     */
    public JwtAuthFilter(JwtProvider jwtProvider, @Lazy MyUserDetailsService userDetailsService) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
    }

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		
		if(exchange.getRequest().getURI().getPath().contains("/auth/refresh")) {
        	
        	return chain.filter(exchange);
        }
		
		final String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // Skip filter if Authorization header is missing or malformed
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
            
        }

        final String token = authHeader.substring(7);
        final String username;
        final String status;
        			
        try {
            username = jwtProvider.extractUsername(token);
            status = jwtProvider.extractAllClaims(token).get("uStatus").toString();
        } catch (Exception e) {
            
            return chain.filter(exchange); // Invalid token, skip authentication
        }

        // Set authentication in security context if not already set
        
        if (username.isEmpty() || status.equalsIgnoreCase("inactive"))
        	return chain.filter(exchange);
        
		return jwtProvider.isBlacklisted(token)
				.flatMap(blocked -> {
					if(!blocked)
			        	return userDetailsService.findByUsername(username)
							//.log("DEBUG-AUTH")
							.flatMap(userDetails ->{
								if(jwtProvider.validToken(token, userDetails)){
			
					                UsernamePasswordAuthenticationToken authenticationToken =
					                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
					                
					                return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authenticationToken));
								}
								
								return chain.filter(exchange);
							})
							.switchIfEmpty(Mono.defer(()->chain.filter(exchange)));
					return chain.filter(exchange);
				});
	}
}
