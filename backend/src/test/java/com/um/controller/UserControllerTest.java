package com.um.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import com.um.configuration.JwtAuthFilter;
import com.um.configuration.JwtProvider;
import com.um.configuration.RateLimitingFilter;
import com.um.dto.UpdateRequest;
import com.um.dto.UserRequest;
import com.um.model.Role;
import com.um.model.Status;
import com.um.model.User;
import com.um.service.MyUserDetailsService;
import com.um.service.RateLimitingService;
import com.um.service.UserService;

import reactor.core.publisher.Mono;

@WebFluxTest(controllers = UserController.class)
class UserControllerTest {

    private WebTestClient webTestClient;

    @MockitoBean
    private JwtProvider jwtProvider;
    
    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;
    
    @MockitoBean
    private RateLimitingFilter rateLimitingFilter;
    
    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RateLimitingService rateLimitingService;
    
    @MockitoBean
    private MyUserDetailsService myUserDetailsService;

    private User u;
    private User u1;
    private Pageable pageable;

    @TestConfiguration
    @EnableWebFluxSecurity
    static class WebFluxTestSecurityConfig {
    	
    	@Bean
    	SecurityWebFilterChain testFilterChain(ServerHttpSecurity http) {
    		return http
    				.csrf(ServerHttpSecurity.CsrfSpec::disable)
    				.authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
    				.build();
    	}
    }
    
    @BeforeEach
    void init(ApplicationContext context) {
        u = new User("Yann", "secure_123", "yannsteve@ymail.fr", Role.ADMIN, Status.ACTIVE);
        u.setId(4L);
        u.setCreatedBy("Yann");
        u.setUpdatedBy("Red");

        u1 = new User("John", "secure_123", "john@free.fr", Role.USER, Status.INACTIVE);
        u1.setId(2L);
        u1.setCreatedBy("Yann");
        u1.setUpdatedBy("Rog");

        pageable = PageRequest.of(0, 10);
        
        when(jwtAuthFilter.filter(any(), any())).thenAnswer(invocation -> {
            ServerWebExchange exchange = invocation.getArgument(0);
            WebFilterChain chain = invocation.getArgument(1);
            return chain.filter(exchange); // Passe au filtre suivant au lieu de renvoyer null
        });

        when(rateLimitingFilter.filter(any(), any())).thenAnswer(invocation -> {
          	ServerWebExchange exchange = invocation.getArgument(0);
            WebFilterChain chain = invocation.getArgument(1);
            return chain.filter(exchange); // Passe au filtre suivant au lieu de renvoyer null
        });
        
        this.webTestClient = WebTestClient.bindToApplicationContext(context)
                .apply(springSecurity()) // <--- Active le support de la sécurité dans le mock
                .configureClient()
                .build();
        
    }

    @Test
    void shouldCreateUser() {
        UserRequest uReq = new UserRequest(u.getUsername(), u.getPassword(), u.getEmail(), u.getRole(), u.getStatus());
        
        when(userService.createUser(any(UserRequest.class), eq("admin"))).thenReturn(Mono.just(u));

        webTestClient.mutateWith(mockUser("admin").roles("ADMIN"))
        		.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(uReq)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.createdBy").isEqualTo("Yann")
                .jsonPath("$.email").isEqualTo("yannsteve@ymail.fr")
                .jsonPath("$.password").doesNotExist()
                .jsonPath("$.status").isEqualTo(Status.ACTIVE.name());

        verify(userService, times(1)).createUser(any(UserRequest.class), eq("admin"));
    }

    @Test
    void getAllUser_shouldReturnallUsers() {
        Page<User> page = new PageImpl<>(List.of(u, u1), pageable, 1);
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(Mono.just(page));

        webTestClient.mutateWith(mockUser("admin").roles("ADMIN"))
        		.get()
                .uri("/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[1].username").isEqualTo("John")
                .jsonPath("$.content[1].password").doesNotExist()
                .jsonPath("$.content[0].role").isEqualTo(Role.ADMIN.name());
    }

    @Test
    void getAllUser_When_Empty() {
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(Mono.just(new PageImpl<>(List.of())));

        webTestClient.mutateWith(mockUser("admin").roles("ADMIN"))
        		.get()
                .uri(uriBuilder -> uriBuilder.path("/users")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .build())
                .exchange()
                .expectStatus().isOk();

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userService).getAllUsers(pageableCaptor.capture());

        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(10, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void test_GetUserById_found() {
        when(userService.getUserById(4L)).thenReturn(Mono.just(u));

        webTestClient.mutateWith(mockUser("admin").roles("ADMIN"))
        		.get()
                .uri("/users/4")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("Yann")
                .jsonPath("$.email").isEqualTo("yannsteve@ymail.fr")
                .jsonPath("$.password").doesNotExist();

        verify(userService, times(1)).getUserById(4L); 
        
    }

    @Test
    void test_GetUserById_notfound() {
        // Dans le contrôleur, si Mono est vide, .defaultIfEmpty(ResponseEntity.notFound().build()) renvoie un 404
        when(userService.getUserById(99L)).thenReturn(Mono.empty());

        webTestClient.mutateWith(mockUser("admin").roles("ADMIN"))
        		.get()
                .uri("/users/99")
                .exchange()
                .expectStatus().isNotFound();

        verify(userService, times(1)).getUserById(99L);
    }

    @Test
    void test_GetUserByMail_found() {
        when(userService.getUserByEmail("john@free.fr")).thenReturn(Mono.just(u1));

        webTestClient.mutateWith(mockUser("admin").roles("ADMIN"))
        		.get()
                .uri("/users/mail/{email}", "john@free.fr")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("John")
                .jsonPath("$.password").doesNotExist();

        verify(userService, times(1)).getUserByEmail("john@free.fr");
    }

    @Test
    void test_GetUserByMail_notfound() {
        when(userService.getUserByEmail("ghost@free.fr")).thenReturn(Mono.empty());

        webTestClient.mutateWith(mockUser("admin").roles("ADMIN"))
        		.get()
                .uri("/users/mail/{email}", "ghost@free.fr")
                .exchange()
                .expectStatus().isNotFound();

        verify(userService, times(1)).getUserByEmail("ghost@free.fr");
    }

    @Test
    void test_UpdateUser_found() {
        UpdateRequest uUp = new UpdateRequest("yannsteve@ymail.fr", Role.ADMIN, Status.ACTIVE);
        
        // le controlleur utilise l'ID (Long) et renvoie le User dans un Mono
        when(userService.updateUser(eq(4L), any(UpdateRequest.class), eq("admin"))).thenReturn(Mono.just(u));

        webTestClient.mutateWith(mockUser("admin").roles("ADMIN"))
        		.put()
                .uri("/users/4")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(uUp)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("Yann")
                .jsonPath("$.password").doesNotExist()
                .jsonPath("$.role").isEqualTo(Role.ADMIN.name());

        verify(userService, times(1)).updateUser(eq(4L), any(UpdateRequest.class), eq("admin"));
    }

    @Test
    void test_UpdateUser_notFound() {
        UpdateRequest uUp = new UpdateRequest("john@free.fr", Role.USER, Status.INACTIVE);
        
        // Si le service renvoie une erreur ou un Mono vide
        when(userService.updateUser(eq(99L), any(UpdateRequest.class), eq("admin"))).thenReturn(Mono.empty());

        webTestClient.mutateWith(mockUser("admin").roles("ADMIN"))
        		.put()
                .uri("/users/99")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(uUp)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void test_DisableUser() {
        // La méthode s'appelle maintenant disableUser et renvoie Mono<Void>
        when(userService.disableUser(2L)).thenReturn(Mono.just(u).then());

        webTestClient.mutateWith(mockUser("admin").roles("ADMIN"))
        		.delete()
                .uri("/users/2")
                .exchange()
                .expectStatus().isNoContent();

        verify(userService, times(1)).disableUser(2L);
    }

}