package com.um.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO representing the response after authentication.
 * Contains an access token for API calls and a refresh token cookie.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema(description = "Response object returned after authentication or refresh")
public class AuthResponse {

    @Schema(description = "JWT access token for authentication in API requests", 
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;


}