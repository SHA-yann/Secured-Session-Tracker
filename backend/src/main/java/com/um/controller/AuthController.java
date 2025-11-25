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

import com.um.dto.AuthRequest;
import com.um.dto.AuthResponse;
import com.um.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
        summary = "Login",
        description = "Authenticates a user with username and password.\n" +
                      "Valid credentials → returns JWT access token and sets refresh cookie.\n" +
                      "Invalid credentials → returns 401 Unauthorized."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful, access token returned"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Authentication request containing username and password", required = true
            )
            @RequestBody AuthRequest request,
            @Parameter(hidden = true) HttpServletResponse response) {

        try {
            AuthResponse authResponse = authService.login(request);
            response.addCookie(authResponse.getRefreshCookie());

            return ResponseEntity.ok(Collections.singletonMap("accessToken", authResponse.getAccessToken()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials");
        }
    }

    @Operation(
        summary = "Refresh token",
        description = "Refreshes authentication using a valid refresh token.\n" +
                      "Valid refresh token → issues new access token and rotates refresh cookie.\n" +
                      "Missing or invalid token → returns 401 Unauthorized.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "New access token issued"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response) {

        AuthResponse authResponse = authService.refresh(request);

        if (authResponse == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("No cookie to refresh authentication");
        }

        response.addCookie(authResponse.getRefreshCookie());
        return ResponseEntity.ok(authResponse);
    }

    @Operation(
        summary = "Logout",
        description = "Logs a user out by revoking refresh tokens and clearing the refresh cookie.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Logout successful, refresh cookie cleared"),
        @ApiResponse(responseCode = "404", description = "User not found or already logged out")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "JSON body containing the username key", required = true
            )
            @RequestBody Map<String, String> req) {

        String username = req.get("username");
        boolean success = authService.logout(username);

        if (!success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found or already logged out");
        }

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE,
                        "refresh_token=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0")
                .build();
    }
}

