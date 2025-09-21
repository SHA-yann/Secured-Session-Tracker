package com.tm.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tm.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * REST controller for authentication operations.
 * Supports login, token refresh, and logout.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Login endpoint.
     * Authenticates a user and returns an access token and refresh cookie.
     *
     * @param request  authentication request containing username and password
     * @param response HTTP response to set refresh cookie
     * @return access token in response body or 401 if credentials are invalid
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request, HttpServletResponse response) {
        try {
            AuthResponse authResponse = authService.login(request);
            response.addCookie(authResponse.getRefreshCookie());

            return ResponseEntity.ok()
                    .body(Collections.singletonMap("accessToken", authResponse.getAccessToken()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials");
        }
    }

    /**
     * Refresh token endpoint.
     * Issues a new access token and refresh cookie if the existing refresh token is valid.
     *
     * @param request  HTTP request containing cookies
     * @param response HTTP response to set new refresh cookie
     * @return new AuthResponse with access token and cookie or 401 if invalid
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.refresh(request);

        if (authResponse == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("No cookie to refresh authentication");
        }

        response.addCookie(authResponse.getRefreshCookie());
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Logout endpoint.
     * Revokes all refresh tokens for the given user and clears the refresh cookie.
     *
     * @param req JSON map containing the "username" key
     * @return 204 No Content if successful, 404 if user not found
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        boolean success = authService.logout(username);

        if (!success) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User not found or already logged out");
        }

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE,
                        "refresh_token=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0")
                .build();
    }
}
