package com.um.controller;

import jakarta.servlet.http.Cookie;
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
public class AuthResponse {

    /** JWT access token for authentication in API requests */
    private String accessToken;

    /** HttpOnly refresh token cookie to obtain new access tokens */
    private Cookie refreshCookie;
}
