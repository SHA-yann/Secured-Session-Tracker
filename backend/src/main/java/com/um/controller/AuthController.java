package com.um.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.um.configuration.CookieProvider;
import com.um.dto.AuthRequest;
import com.um.dto.AuthResponse;
import com.um.service.AuthService;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * REST controller handling authentication operations:
 * <ul>
 *     <li>Login with credentials</li>
 *     <li>Refreshing JWT access tokens</li>
 *     <li>Logging out and revoking refresh tokens</li>
 * </ul>
 */
@RestController
@Slf4j
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
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
        @ApiResponse(responseCode = "200", description = "Login successful, access token returned", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Authentication request containing username and password", required = true
            )
            @RequestBody AuthRequest request,
            @Parameter(hidden = true) ServerWebExchange exchange) {

        	return authService.login(request).map(res -> {exchange.getResponse().addCookie(res.cookie());
											        	return ResponseEntity.ok(new AuthResponse(res.token()));
														})
        										.onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        																.build()));
        	
        
    }

    @Operation(
        summary = "Refresh token",
        description = "Refreshes authentication using a valid refresh token.\n" +
                      "Valid refresh token → issues new access token and rotates refresh cookie.\n" +
                      "Missing or invalid token → returns 401 Unauthorized.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "New access token issued", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid refresh token")
    })
    @PostMapping("/refresh")
    public Mono<ResponseEntity<AuthResponse>> refresh(@CookieValue(name = "refresh_token",required = false) String refreshToken,
            ServerWebExchange exchange) {

    	if (refreshToken == null || refreshToken.isEmpty()) {
    		log.warn("No refresh cookie found!");
    		return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    	}
    	
        return authService.refresh(refreshToken)
						  .map(res -> {exchange.getResponse().addCookie(res.cookie());
							return ResponseEntity.ok(new AuthResponse(res.token()));
						   });
        		
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
    public Mono<ResponseEntity<Void>> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken, ServerWebExchange exchange) {

               exchange.getResponse().beforeCommit(() -> Mono.fromRunnable(() ->
	            	   exchange.getResponse().addCookie(CookieProvider.clearCookie("refresh_token"))
	               ));
	          
               return authService.logout(refreshToken)
            		   .then(Mono.just(ResponseEntity.noContent().build()));
               
    }
}

