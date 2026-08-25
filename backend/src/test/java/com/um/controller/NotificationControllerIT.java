package com.um.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.um.configuration.AbstractIntegrationTest;
import com.um.dto.PresenceDTO;
import com.um.model.Role;
import com.um.model.Status;
import com.um.model.User;
import com.um.service.UserService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "20000") // Laisse une marge de 20s pour la gestion réactive
class NotificationControllerIT extends AbstractIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(username = "Yann")
    @DisplayName("Devrait ouvrir le flux SSE, renvoyer la liste des connectés et s'abonner aux deltas")
    void streamNotifications_shouldReturnEventStreamWhenAuthenticated() {
        // GIVEN
        User mockUser = new User("Yann", "password", "yannsteve@ymail.fr", Role.ADMIN, Status.ACTIVE);
        mockUser.setId(40L);

        when(userService.getUserByUsername("Yann")).thenReturn(Mono.just(mockUser));

        // Paramétrage du type cible pour désérialiser proprement le ServerSentEvent<PresenceDTO>
        ParameterizedTypeReference<ServerSentEvent<PresenceDTO>> sseType = 
                new ParameterizedTypeReference<>() {};

        // WHEN
        Flux<ServerSentEvent<PresenceDTO>> sseStream = webTestClient.get()
                .uri("/notifications/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                // THEN
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectHeader().valueEquals("X-Accel-Buffering", "no")
                .expectHeader().valueEquals("Cache-Control", "no-cache")
                .returnResult(sseType)
                .getResponseBody();

        // Évaluation réactive du comportement avec StepVerifier
        StepVerifier.create(sseStream)
                .assertNext(event -> {
                    // L'utilisateur authentifié s'ajoute lui-même à Redis, la liste initiale contient donc au moins Yann
                    assertThat(event.event()).isEqualTo("presence-update");
                    PresenceDTO data = event.data();
                    assertThat(data).isNotNull();
                    assertThat(data.Id()).isEqualTo(40L);
                    assertThat(data.name()).isEqualTo("Yann");
                    assertThat(data.status()).isEqualTo("CONNECTED");
                })
                .thenCancel() // Annulation requise : le flux fusionne avec un interval infini (keep-alive)
                .verify(Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("Devrait renvoyer une erreur 401 Unauthorized si l'utilisateur n'est pas authentifié")
    void streamNotifications_shouldReturn401WhenAnonymous() {
        // En l'absence de l'annotation @WithMockUser, le SecurityContext réactif sera vide
        webTestClient.get()
                .uri("/notifications/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isUnauthorized(); // Valide le switchIfEmpty de ton contrôleur
    }
}