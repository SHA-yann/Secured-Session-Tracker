package com.um.service;

import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.um.configuration.CookieProvider;
import com.um.configuration.JwtProvider;
import com.um.controller.AuthRequest;
import com.um.controller.AuthResponse;
import com.um.model.RefreshToken;
import com.um.model.User;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Service handling user authentication, JWT issuance, refresh token rotation,
 * and logout operations.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final MyUserDetailsService myUserDetailsService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${security.cookie.domain}")
    private String cookieDomain;

    @Value("${security.cookie.secure}")
    private boolean cookieSecure;

    private static final String REFRESH_COOKIE = "refresh_token";

    public AuthService(AuthenticationManager authenticationManager,
                       UserService userService,
                       JwtProvider jwtProvider,
                       MyUserDetailsService myUserDetailsService,
                       RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtProvider = jwtProvider;
        this.myUserDetailsService = myUserDetailsService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Authenticates a user and issues an access token and refresh token cookie.
     *
     * @param request login credentials
     * @return AuthResponse containing JWT and refresh cookie
     */
    public AuthResponse login(AuthRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String accessToken = jwtProvider.generateToken((UserDetails) auth.getPrincipal());
        RefreshToken rt = refreshTokenService.issue(userService.findByName(auth.getName()).get());

        int maxAge = (int) (rt.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond());
        Cookie refreshCookie = CookieProvider.createCookie(REFRESH_COOKIE, rt.getToken(), cookieDomain, cookieSecure, maxAge);

        return new AuthResponse(accessToken, refreshCookie);
    }

    /**
     * Refreshes the access token using a valid refresh token and rotates it.
     *
     * @param request HTTP request containing the refresh cookie
     * @return AuthResponse with new JWT and refresh cookie
     */
    public AuthResponse refresh(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        String raw = null;
        for (Cookie c : cookies) {
            if (REFRESH_COOKIE.equals(c.getName())) {
                raw = c.getValue();
                break;
            }
        }
        if (raw == null || raw.isBlank()) return null;

        RefreshToken currentRt = refreshTokenService.verify(raw);
        RefreshToken nextRt = refreshTokenService.rotate(currentRt);

        String username = currentRt.getUser().getUsername();
        String newAccess = jwtProvider.generateToken(myUserDetailsService.loadUserByUsername(username));

        int maxAge = (int) (nextRt.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond());
        Cookie newRefreshCookie = CookieProvider.createCookie(REFRESH_COOKIE, nextRt.getToken(), cookieDomain, cookieSecure, maxAge);

        return new AuthResponse(newAccess, newRefreshCookie);
    }

    /**
     * Logs out a user by revoking all their refresh tokens.
     *
     * @param username identifier of the user
     * @return true if successful, false if user not found
     */
    public boolean logout(String username) {
        Optional<User> user = userService.findByName(username);
        if (user.isPresent()) {
            refreshTokenService.revokeUserTokens(user.get().getId());
            return true;
        }
        return false;
    }
}
