package com.tm.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tm.service.MyUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT Authentication filter that intercepts each request once,
 * extracts the JWT token from the Authorization header,
 * validates it, and sets the security context accordingly.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final MyUserDetailsService userDetailsService;

    /**
     * Constructor injecting dependencies.
     *
     * @param jwtProvider JWT utility for token parsing and validation
     * @param userDetailsService Service to load user details
     */
    public JwtAuthFilter(JwtProvider jwtProvider, MyUserDetailsService userDetailsService) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Filters incoming HTTP requests to authenticate JWT tokens.
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain filter chain
     * @throws ServletException in case of servlet errors
     * @throws IOException in case of I/O errors
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Skip filter if Authorization header is missing or malformed
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        final String username;

        try {
            username = jwtProvider.extractUsername(token);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return; // Invalid token, skip authentication
        }

        // Set authentication in security context if not already set
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtProvider.validToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
