package com.um.controller;

import com.um.dto.AuthRequest;
import com.um.service.RateLimitingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.stream.IntStream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class RateLimitingIT {

    @Autowired
    private WebTestClient webTestClient;
    
    @Autowired
    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        rateLimitingService.clearBuckets(); // we clear buckets !
    }

    @Test
    @DisplayName("Rate Limit - Sensitive Plan: Should return 429 Too Many Requests once capacity is depleted")
    void shouldReturn429WhenSensitiveQuotaExceeded() {
        String sensitivePath = "/auth/login";
        
        // We target the capacity defined in RateLimitingPlan.SENSITIVE (10 requests)
        int allowedCapacity = 10; 
        AuthRequest dummyRequest = new AuthRequest("Yann", "password123");
        
        // 1. Consume all allowed tokens rapidly using a synchronous-like stream in WebTestClient
        IntStream.range(0, allowedCapacity).forEach(i -> 
            webTestClient.post()
                .uri(sensitivePath)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dummyRequest)
                .exchange()
                // It can return 200 or 401 depending on the user status, but it must NOT be 429 yet
                .expectStatus().value(status -> org.assertj.core.api.Assertions.assertThat(status)
                							.isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS.value())
                					)
        );

        // 2. The 11th request must hit the rate limit constraint and return 429
        webTestClient.post()
            .uri(sensitivePath)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(dummyRequest)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
            .expectBody()
            // Adjust the jsonPath according to your Global Exception Handler (@RestControllerAdvice)
            .jsonPath("$.message").isEqualTo("Too many requests, please wait");
    }
    
}