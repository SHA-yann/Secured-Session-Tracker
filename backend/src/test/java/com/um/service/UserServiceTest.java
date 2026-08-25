package com.um.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.um.Exceptions.UserAlreadyExistsException;
import com.um.Exceptions.UserNotFoundException;
import com.um.dto.UpdateRequest;
import com.um.dto.UserRequest;
import com.um.model.Role;
import com.um.model.Status;
import com.um.model.User;
import com.um.repository.UserRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserService userService;

    private User toSave;
    private User u1;
    private Pageable pageable;

    @BeforeEach
    void init() {
        toSave = new User("Yann", "secure_123", "yannsteve@ymail.fr", Role.ADMIN, Status.ACTIVE);
        u1 = new User("John", "secure_123", "john@free.fr", Role.USER, Status.ACTIVE);
        pageable = PageRequest.of(0, 2, Sort.by("username").ascending());
    }

    // ==========================================
    // -------------------- CREATE --------------
    // ==========================================

    @Test
    @DisplayName("Should create user successfully when username is available")
    void createUser_shouldPersistAndReturnEntity() {
        // Given
        UserRequest dto = new UserRequest("Yann", "secure_123", "yannsteve@ymail.fr", Role.ADMIN, Status.ACTIVE);
        when(userRepository.existsByUsername("Yann")).thenReturn(false);
        when(passwordEncoder.encode("secure_123")).thenReturn("hashed_123");
        when(userRepository.save(any(User.class))).thenReturn(toSave);

        // When & Then
        StepVerifier.create(userService.createUser(dto, "System")).assertNext(user -> {
                    org.assertj.core.api.Assertions.assertThat(user).isNotNull();
                    org.assertj.core.api.Assertions.assertThat(user.getUsername()).isEqualTo("Yann");
                })
                .verifyComplete();

        org.mockito.Mockito.verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should return error signal when trying to create an existing user")
    void createUser_shouldFailWhenUserAlreadyExists() {
        // Given
        UserRequest dto = new UserRequest("Yann", "secure_123", "yannsteve@ymail.fr", Role.ADMIN, Status.ACTIVE);
        when(userRepository.existsByUsername("Yann")).thenReturn(true);

        // When & Then
        StepVerifier.create(userService.createUser(dto, "System"))
                .verifyError(UserAlreadyExistsException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    // ==========================================
    // -------------------- READ ALL ------------
    // ==========================================

    @Test
    @DisplayName("Should return a reactive page stream of all users")
    void getAllUsers_shouldReturnPage() {
        // Given
        Page<User> page = new PageImpl<>(List.of(toSave, u1), pageable, 2);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When & Then
        StepVerifier.create(userService.getAllUsers(pageable))
                .assertNext(resultPage -> {
                    org.assertj.core.api.Assertions.assertThat(resultPage.getContent()).hasSize(2);
                    org.assertj.core.api.Assertions.assertThat(resultPage.getContent().get(1).getUsername()).isEqualTo("John");
                })
                .verifyComplete();
    }

    // ==========================================
    // -------------------- READ BY ID ----------
    // ==========================================

    @Test
    @DisplayName("Should return user object when requested ID is found")
    void getUserById_found() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(toSave));

        // When & Then
        StepVerifier.create(userService.getUserById(1L))
                .assertNext(user -> org.assertj.core.api.Assertions.assertThat(user.getUsername()).isEqualTo("Yann"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return error signal when user ID is not found")
    void getUserById_notFound() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        StepVerifier.create(userService.getUserById(99L))
                .verifyError(UserNotFoundException.class);
    }

    // ==========================================
    // -------------------- UPDATE --------------
    // ==========================================

    @Test
    @DisplayName("Should allow Admin to update any user information and roles")
    void updateUser_asAdmin_shouldSucceed() {
        // Given
        UpdateRequest updateDto = new UpdateRequest("updated@free.fr", Role.USER, Status.ACTIVE);
        
        // Mocks pour simuler un contexte de sécurité réactif avec rôle ADMIN
        Authentication authentication = mock(Authentication.class);
        when(authentication.getAuthorities()).thenAnswer(inv -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(authentication.getName()).thenReturn("AdminUser");
        
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        when(userRepository.findById(2L)).thenReturn(Optional.of(u1)); // John
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Contextes réactifs mockés via le pipeline Reactor
        Mono<User> resultMono = userService.updateUser(2L, updateDto, "AdminUser")
        		.contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));

        // When & Then
        StepVerifier.create(resultMono)
                .assertNext(updatedUser -> {
                    org.assertj.core.api.Assertions.assertThat(updatedUser.getEmail()).isEqualTo("updated@free.fr");
                    org.assertj.core.api.Assertions.assertThat(updatedUser.getUpdatedBy()).isEqualTo("AdminUser");
                })
                .verifyComplete();
    }

    // ==========================================
    // -------------------- DISABLE -------------
    // ==========================================

    @Test
    @DisplayName("Should set status to INACTIVE and revoke tokens when disabling user")
    void disableUser_shouldSucceedWhenUserExists() {
        // Given
    	toSave.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(toSave));
        when(userRepository.save(any(User.class))).thenReturn(toSave);
        when(refreshTokenService.revokeUserTokens(anyLong())).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(userService.disableUser(1L))
                .verifyComplete();

        org.assertj.core.api.Assertions.assertThat(toSave.getStatus()).isEqualTo(Status.INACTIVE);
        verify(refreshTokenService, times(1)).revokeUserTokens(anyLong());
        verify(userRepository, times(1)).save(toSave);
    }

    // ==========================================
    // -------------------- SEARCH --------------
    // ==========================================

    @Test
    @DisplayName("Should query matching method when username filter is present")
    void searchUsers_withUsername() {
        // Given
        Page<User> expectedPage = new PageImpl<>(List.of(toSave));
        when(userRepository.findByUsernameContainingIgnoreCase("Yann", pageable)).thenReturn(expectedPage);

        // When & Then
        StepVerifier.create(userService.searchUsers("Yann", null, pageable))
                .assertNext(page -> org.assertj.core.api.Assertions.assertThat(page.getContent()).contains(toSave))
                .verifyComplete();

        verify(userRepository, times(1)).findByUsernameContainingIgnoreCase("Yann", pageable);
        verify(userRepository, never()).findByRole(any(), any());
    }
}