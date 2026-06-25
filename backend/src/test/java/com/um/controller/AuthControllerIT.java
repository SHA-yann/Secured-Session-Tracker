package com.um.controller;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.github.dockerjava.api.model.AuthResponse;
import com.um.dto.AuthRequest;
import com.um.model.Role;
import com.um.model.Status;
import com.um.model.User;
import com.um.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class AuthControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User user = new User("Yann", passwordEncoder.encode("password123"), "yannsteve@gmail.com", Role.ADMIN, Status.ACTIVE);
        user.setCreatedBy("System");
        user.setUpdatedBy("System");
        userRepository.save(user);
        
    }

    @Test
    @DisplayName("Login Success: Should return Access Token and HttpOnly Refresh Cookie")
    void login_Success_ReturnsTokenAndCookie() {
        AuthRequest request = new AuthRequest("Yann", "password123");

        webTestClient.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").exists()
                .consumeWith(result -> {
                    var cookie = result.getResponseHeaders().getFirst("Set-Cookie");
                    assertThat(cookie).contains(REFRESH_TOKEN_COOKIE);
                    assertThat(cookie).contains("HttpOnly");
                });
    }

    @Test
    @DisplayName("Login Invalid: Should return 401 Unauthorized for wrong password")
    void login_InvalidPassword_Returns401() {
        AuthRequest request = new AuthRequest("Yann", "wrong_password");

        webTestClient.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Refresh Success: Should rotate token and return new access token")
    void refresh_Success_ReturnsNewToken() {
        // 1. Get initial cookie
        var loginResponse = webTestClient.post().uri("/auth/login")
                .bodyValue(new AuthRequest("Yann", "password123"))
                .exchange()
                .expectStatus().isOk()
                .returnResult(AuthResponse.class);
        
        var cookie = loginResponse.getResponseCookies().getFirst(REFRESH_TOKEN_COOKIE);

        // 2. Perform refresh
        webTestClient.post().uri("/auth/refresh")
                .cookie(REFRESH_TOKEN_COOKIE, cookie.getValue())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").exists();
    }

    @Test
    @DisplayName("Refresh Without Token: Should return 401 Unauthorized")
    void refresh_NoCookie_Returns401() {
        webTestClient.post().uri("/auth/refresh")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Logout - Success: Should clear client cookie and return 204 when valid cookie is provided")
    void logout_ShouldClearCookieAndReturn204_WhenValidCookieProvided() {
        // Step 1: Perform a valid login to capture a real system-generated refresh token cookie
        var loginResult = webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new AuthRequest("Yann", "password123"))
                .exchange()
                .expectStatus().isOk()
                .returnResult(AuthResponse.class);

        var refreshCookie = loginResult.getResponseCookies().getFirst("refresh_token");
        assert refreshCookie != null : "Refresh token cookie must be present upon login success";

        // Step 2: Execute logout using the extracted cookie
        webTestClient.post()
                .uri("/auth/logout")
                .cookie("refresh_token", refreshCookie.getValue())
                .exchange()
                .expectStatus().isNoContent() // Expect 204 No Content
                .expectHeader().valueEquals(HttpHeaders.SET_COOKIE, 
                        "refresh_token=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0");
    }

    @Test
    @DisplayName("Logout - Security: Should still clear client cookie and return 204 when cookie is invalid or missing")
    void logout_ShouldStillReturn204_WhenCookieIsInvalidOrMissing() {
        // Sending an invalid/expired token or completely omitting it 
        // Must yield the exact same response to prevent user enumeration attacks
        webTestClient.post()
                .uri("/auth/logout")
                .cookie("refresh_token", "invalid-or-already-evicted-token")
                .exchange()
                .expectStatus().isNoContent() // Security constraint: Always return 204
                .expectHeader().valueEquals(HttpHeaders.SET_COOKIE, 
                        "refresh_token=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0");
    }
}