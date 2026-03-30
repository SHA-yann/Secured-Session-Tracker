package com.um.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class RateLimitingIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Devrait bloquer la requête avec un code 429 après avoir épuisé le quota SENSITIVE")
    void shouldReturn429WhenQuotaExceeded() throws Exception {
        String sensitivePath = "/auth/login";
        int limit = 10; // Capacité du plan SENSITIVE dans votre code

        // 1. On consomme tous les jetons autorisés
        for (int i = 0; i < limit; i++) {
            mockMvc.perform(post(sensitivePath));
                   
        }

        // 2. La 11ème requête doit être rejetée
        mockMvc.perform(post(sensitivePath))
               .andExpect(status().isTooManyRequests())
               .andExpect(jsonPath("$.error").value("Trop de requêtes!...Réessayez dans 1 minute."));
    }

    @Test
    @DisplayName("Devrait ignorer le rate limiting pour les ressources Swagger")
    void shouldIgnoreSwaggerResources() throws Exception {
    	mockMvc.perform(get("/v3/api-docs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
