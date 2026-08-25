package com.um.configuration;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.um.service.MyUserDetailsService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpHeaders;

/**
 * JWT Authentication filter that intercepts each request once,
 * extracts the JWT token from the Authorization header,
 * validates it, and sets the security context accordingly.
 */
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
    public JwtAuthFilter(JwtProvider jwtProvider,  @Lazy MyUserDetailsService userDetailsService) {
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
					if(blocked) {
						return chain.filter(exchange);
					}
					
		        	return userDetailsService.findByUsername(username)
						.flatMap(userDetails ->{
							if(jwtProvider.validToken(token, userDetails)){
		
				                UsernamePasswordAuthenticationToken authenticationToken =
				                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				                
				                return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authenticationToken));
							}
							
							return chain.filter(exchange);
						})
						.switchIfEmpty(Mono.defer(()->chain.filter(exchange)));
				});
	}
}
