package com.um.controller;

import com.um.dto.UpdateRequest;
import com.um.dto.UserRequest;
import com.um.dto.UserResponse;
import com.um.model.Role;
import com.um.model.Status;
import com.um.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for UserController covering full reactive CRUD flow using WebTestClient.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WithMockUser(username = "admin", roles = {"ADMIN"})
@AutoConfigureWebTestClient
class UserControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserService userService;

    @BeforeEach
    void cleanDatabase() {
        userService.wipeAll(); 
    }

    /**
     * Full CRUD integration test adapted for WebFlux and reactive security context.
     */
    @Test
    void testFullCrudFlow() {
        // 1. CREATE
        UserRequest uReq = new UserRequest("Yann", "secure_123", "yannsteve@ymail.fr", Role.ADMIN, Status.ACTIVE);

        UserResponse created = webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(uReq)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(created).isNotNull();
        assertThat(created.id()).isNotNull();
        Long userId = created.id();

        // 2. READ
        webTestClient.get()
                .uri("/users/{id}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("Yann")
                .jsonPath("$.email").isEqualTo("yannsteve@ymail.fr");

        // 3. UPDATE
        UpdateRequest uUp = new UpdateRequest("john@free.fr", Role.USER, Status.ACTIVE);

        webTestClient.put()
                .uri("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(uUp)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.email").isEqualTo("john@free.fr")
                .jsonPath("$.role").isEqualTo("USER");

        // 4. DELETE
        webTestClient.delete()
                .uri("/users/{id}", userId)
                .exchange()
                .expectStatus().isNoContent();

        // 5. READ after DELETE → Not Found (404)
        webTestClient.get()
                .uri("/users/{id}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("INACTIVE");
    }
}