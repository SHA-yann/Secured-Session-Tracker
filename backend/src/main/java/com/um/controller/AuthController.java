package com.um.controller;

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

import com.um.DTOs.AuthRequest;
import com.um.DTOs.AuthResponse;
import com.um.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * REST controller handling authentication operations:
 * <ul>
 *     <li>Login with credentials</li>
 *     <li>Refreshing JWT access tokens</li>
 *     <li>Logging out and revoking refresh tokens</li>
 * </ul>
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for login, refresh, and logout")
public class AuthController {

    private final AuthService authService;

    /**
     * Creates a new {@code AuthController}.
     *
     * @param authService service providing authentication logic
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticates a user with username and password.
     * <ul>
     *     <li>Valid credentials → returns JWT access token and sets refresh cookie</li>
     *     <li>Invalid credentials → returns 401 Unauthorized</li>
     * </ul>
     *
     * @param request  the authentication request containing username and password
     * @param response the HTTP response, used to add the refresh token cookie
     * @return 200 OK with access token or 401 Unauthorized if credentials are invalid
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
     * Refreshes authentication using a valid refresh token.
     * <ul>
     *     <li>Valid refresh token → issues new access token and rotates refresh cookie</li>
     *     <li>Missing or invalid token → returns 401 Unauthorized</li>
     * </ul>
     *
     * @param request  the HTTP request containing cookies
     * @param response the HTTP response, used to set the new refresh token cookie
     * @return 200 OK with new {@link AuthResponse} or 401 Unauthorized
     */
    @PostMapping("/refresh")
    @Operation(security=@SecurityRequirement(name = "bearerAuth"))
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
     * Logs a user out by revoking refresh tokens and clearing the refresh cookie.
     *
     * @param req JSON body containing the {@code username} key
     * @return 204 No Content if successful, 404 Not Found if the user does not exist or is already logged out
     */
    @PostMapping("/logout")
    @Operation(security=@SecurityRequirement(name = "bearerAuth"))
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
