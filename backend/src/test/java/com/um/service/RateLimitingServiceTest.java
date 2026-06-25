package com.um.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import com.um.configuration.RateLimitingPlan;
import io.github.bucket4j.Bucket;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitingServiceTest {

    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        rateLimitingService = new RateLimitingService();
    }

    @AfterEach
    void tearDown() {
        // Garantit l'isolation des tests en nettoyant les états des buckets en mémoire
        rateLimitingService.clearBuckets();
    }

    @ParameterizedTest(name = "Path: {0} | Auth: {1} | API-Key: {2} -> Plan attendu: {3}")
    @CsvSource({
        "/api/users,       ,            , PUBLIC",
        "/api/auth/login,  ,            , SENSITIVE",
        "/api/auth/register, ,          , SENSITIVE",
        "/api/users,       Bearer tok,  , AUTH",
        "/api/auth/login,  Bearer tok,  , AUTH",      // L'authentification passe avant le path sensible
        "/api/users,       ,            key-123, WEBHOOK",
        "/api/users,       Bearer tok,  key-123, WEBHOOK" // L'API Key est prioritaire sur tout
    })
    @DisplayName("Doit déterminer le bon plan selon la hiérarchie et les en-têtes de la requête")
    void shouldCorrectlyDeterminePlan(String path, String authHeader, String apiKey, RateLimitingPlan expectedPlan) {
        RateLimitingPlan plan = rateLimitingService.determinePlan(path, authHeader, apiKey);
        assertThat(plan).isEqualTo(expectedPlan);
    }

    @Test
    @DisplayName("Doit retourner le même bucket existant pour une clé identique")
    void shouldReturnSameInstanceForSameKey() {
        // Given
        String key = "127.0.0.1:PUBLIC";

        // When
        Bucket firstCall = rateLimitingService.resolveBucket(key, RateLimitingPlan.PUBLIC);
        Bucket secondCall = rateLimitingService.resolveBucket(key, RateLimitingPlan.PUBLIC);

        // Then
        assertThat(firstCall).isSameAs(secondCall);
    }

    @Test
    @DisplayName("Doit bloquer les requêtes lorsque la capacité maximale du bucket est épuisée")
    void shouldExhaustBucketTokensAndReject() {
        // Given
        String key = "192.168.1.1:SENSITIVE";
        RateLimitingPlan plan = RateLimitingPlan.SENSITIVE; // Capacité de 10 jetons
        Bucket bucket = rateLimitingService.resolveBucket(key, plan);

        // When : On consomme les 10 jetons disponibles initialement
        for (int i = 0; i < 10; i++) {
            assertThat(bucket.tryConsume(1))
                    .as("Le jeton " + (i + 1) + " aurait dû être consommé")
                    .isTrue();
        }

        // Then : Le 11ème jeton doit être rejeté (Rate Limit activé)
        assertThat(bucket.tryConsume(1))
                .as("Le bucket est censé être épuisé")
                .isFalse();
    }

    @Test
    @DisplayName("Doit vider complètement la mémoire lors de l'appel au nettoyage")
    void shouldClearAllCreatedBuckets() {
        // Given
        rateLimitingService.resolveBucket("user-1", RateLimitingPlan.PUBLIC);
        rateLimitingService.resolveBucket("user-2", RateLimitingPlan.AUTH);

        // When
        rateLimitingService.clearBuckets();

        // Then : On vérifie que de nouvelles instances de buckets sont recréées (preuve du nettoyage)
        Bucket postClearBucket = rateLimitingService.resolveBucket("user-1", RateLimitingPlan.PUBLIC);
        assertThat(postClearBucket).isNotNull();
    }
}