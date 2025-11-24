package com.um.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.um.DTOs.UserRequest;
import com.um.Exceptions.UserNotFoundException;
import com.um.model.Role;
import com.um.model.Status;
import com.um.model.User;
import com.um.repository.UserRepository;
import java.util.List;
import java.util.Optional;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User toSave;
    private User u1;
    private Pageable pageable;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);

        // Initialize test users
        toSave = new User("Yann","secure_123", "yannsteve@ymail.fr",Role.ADMIN,Status.ACTIVE);
        u1 = new User("John","secure_123","john@free.fr",Role.USER,Status.ACTIVE);
        
        // Set up pagination for retrieval tests
        pageable = PageRequest.of(0, 2, Sort.by("username").ascending());

        // Set SecurityContext for tests requiring authentication
        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    // -------------------- CREATE --------------------
    @Test
    void createUser_shouldPersistAndReturnEntity() {
        // Mock the save operation to return the user
        when(userRepository.save(any(User.class))).thenReturn(toSave);

        User result = userRepository.save(toSave);
        result.setStatus(Status.ACTIVE);

        // Validate that the user was saved correctly
        assertNotNull(result);
        assertEquals("Yann", result.getUsername());
        assertEquals("yannsteve@ymail.fr", result.getEmail());
        assertEquals(Status.ACTIVE, result.getStatus());

        // Verify that save was invoked once
        verify(userRepository, times(1)).save(any(User.class));
    }

    // -------------------- READ ALL --------------------
    @Test
    void test_souldreturnAllusers() {
        // Assign roles and create page result
        toSave.setStatus(Status.ACTIVE);
        u1.setStatus(Status.INACTIVE);
        Page<User> page = new PageImpl<>(List.of(toSave, u1), pageable, 2);

        // Mock repository call
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<User> result = userService.getAllUsers(pageable);

        // Verify correct data returned
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(1).getUsername()).isEqualTo("John");
        assertThat(result.getContent().get(1).getStatus()).isEqualTo(Status.INACTIVE);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(Status.ACTIVE);

        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    void test_when_emptyList() {
        // Mock empty result
        when(userRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        Page<User> result = userService.getAllUsers(pageable);

        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findAll(pageable);
    }

    // -------------------- READ BY ID --------------------
    @Test
    void test_foundById() {
        // Mock finding user by ID
        when(userRepository.findById(2L)).thenReturn(Optional.of(u1));

        Optional<User> result = Optional.of(userService.getUserById(2L).get());

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@free.fr");

        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    void test_notFoundById() {
        // Mock user not found scenario
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(2L));

        verify(userRepository, times(1)).findById(2L);
    }

    // -------------------- READ BY EMAIL --------------------
    @Test
    void test_foundByEmail() {
        when(userRepository.findByEmail("john@free.fr")).thenReturn(Optional.of(u1));

        Optional<User> result = Optional.of(userService.getUserByEmail("john@free.fr").get());

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("John");

        verify(userRepository, times(1)).findByEmail("john@free.fr");
    }

    @Test
    void test_notfoundByEmail() {
        when(userRepository.findByEmail("frank@wanado.fr")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail("frank@wanado.fr"));

        verify(userRepository, times(1)).findByEmail("frank@wanado.fr");
    }

    // -------------------- UPDATE --------------------
    @Test
    void test_UpdateUser_found() {
        u1.setStatus(Status.INACTIVE);

        // Mock find and save operations
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(toSave));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserRequest uReq= new UserRequest(u1.getUsername(), u1.getPassword(), "frank@wanado.fr", Role.USER, u1.getStatus());
        Optional<User> result = userService.updateUser("Yann",uReq,"John");

        // Validate updated user
        assertThat(result).isNotNull();
        assertThat(result.get().getEmail()).isEqualTo("frank@wanado.fr");
        assertThat(result.get().getStatus()).isEqualTo(Status.ACTIVE); // unchanged
        assertThat(result.get().getUpdatedBy()).isEqualTo("John");

        verify(userRepository, times(1)).findByUsername(any(String.class));
        verify(userRepository, times(1)).save(toSave);
    }

    @Test
    void test_UpdateUser_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UserRequest uReq= new UserRequest(u1.getUsername(), u1.getPassword(), u1.getEmail(), u1.getRole(), u1.getStatus());
        assertThrows(UserNotFoundException.class, () -> userService.updateUser("none", uReq,"Yann"));

        verify(userRepository, times(1)).findByUsername(any(String.class));
        verify(userRepository, never()).save(any(User.class));
    }

    // -------------------- DELETE --------------------
    @Test
    void testDelUser_found() {
        when(userRepository.existsById(3L)).thenReturn(true);

        userService.deleteUser(3L);

        verify(userRepository, times(1)).existsById(3L);
        verify(userRepository, times(1)).deleteById(3L);
    }

    @Test
    void testDelUser_notFound() {
        when(userRepository.existsById(99L)).thenThrow(new UserNotFoundException(""));

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(99L));

        verify(userRepository, times(1)).existsById(99L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    // -------------------- SEARCH --------------------
    @Test
    void searchUsers_withusername() {
        Page<User> expected = new PageImpl<>(List.of(toSave, u1));

        when(userRepository.findByUsernameContainingIgnoreCase("Yann", pageable)).thenReturn(expected);

        Page<User> result = userService.searchUsers("Yann", null, pageable);

        // Validate search by username
        assertThat(result.getContent()).hasSize(2).contains(toSave);

        // Ensure repository calls are correct
        verify(userRepository, times(1)).findByUsernameContainingIgnoreCase("Yann", pageable);
        verify(userRepository, never()).findByRole(any(), any());
        verify(userRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void searchUsers_withRole() {
        toSave.setRole(Role.ADMIN);
        Page<User> expected = new PageImpl<>(List.of(toSave, u1));

        when(userRepository.findByRole(Role.USER, pageable)).thenReturn(expected);

        Page<User> result = userService.searchUsers(null, Role.USER, pageable);

        assertThat(result.getContent()).hasSize(2).contains(u1);

        verify(userRepository, times(1)).findByRole(Role.USER, pageable);
        verify(userRepository, never()).findByUsernameContainingIgnoreCase(any(), any());
        verify(userRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void search_users_withoutFilters() {
        Page<User> expected = new PageImpl<>(List.of(toSave, u1));

        when(userRepository.findAll(pageable)).thenReturn(expected);

        Page<User> result = userService.searchUsers(null, null, pageable);

        assertThat(result.getContent()).hasSize(2).contains(toSave, u1);

        verify(userRepository, times(1)).findAll(pageable);
        verify(userRepository, never()).findByUsernameContainingIgnoreCase(any(), any());
        verify(userRepository, never()).findByRole(any(), any());
    }
}
