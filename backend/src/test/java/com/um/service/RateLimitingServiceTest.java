package com.um.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.um.configuration.RateLimitingPlan;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitingServiceTest {

    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        rateLimitingService = new RateLimitingService();
    }

    @Test
    @DisplayName("Doit retourner le plan SENSITIVE pour les routes d'authentification")
    void shouldReturnSensitivePlan() {
        RateLimitingPlan plan = rateLimitingService.determinePlan("/api/auth/login", null, null);
        assertThat(plan).isEqualTo(RateLimitingPlan.SENSITIVE);
    }

    @Test
    @DisplayName("Doit prioriser le plan WEBHOOK si une API Key est présente")
    void shouldPrioritizeWebhookPlan() {
        RateLimitingPlan plan = rateLimitingService.determinePlan("/api/data", "Bearer token", "key-123");
        assertThat(plan).isEqualTo(RateLimitingPlan.WEBHOOK);
    }

    @Test
    @DisplayName("Doit retourner un bucket fonctionnel pour une clé donnée")
    void shouldResolveAndConsumeBucket() {
        var bucket = rateLimitingService.resolveBucket("127.0.0.1:PUBLIC", RateLimitingPlan.PUBLIC);
        assertThat(bucket).isNotNull();
        assertThat(bucket.tryConsume(1)).isTrue();
    }
}
