package com.um.dto;

import org.springframework.http.ResponseCookie;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO representing the response after authentication.
 * Contains an access token for API calls and a refresh token cookie.
 */
@Schema(description = "Response object returned after authentication or refresh")
public record AuthResponse(String accessToken) {
	
}
