package com.tm.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.um.model.Role;
import com.um.model.User;
import com.um.repository.UserRepository;

@DataJpaTest // Spins up an in-memory database for repository testing only, without loading the full Spring context
class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository; // The repository under test
    
    private User u1;
    private User u2;

    @BeforeEach
    void setup() {
        // Initialize test users before each test
        u2 = new User("Yann","yannsteve@ymail.fr","secure_123");
        u1 = new User("john","john@free.fr","secure_123");
    }

    @Test
    void shouldSaveAndFindUserWithPasswordAndRole() {
        // GIVEN a user with a role
        u2.setRole(Role.ADMIN);

        // WHEN saving the user
        User saved = userRepository.save(u2);

        // THEN it can be retrieved and fields match
        Optional<User> found = userRepository.findById(saved.getId());
        assertNotNull(found);
        assertEquals("Yann", found.get().getUsername());
        assertEquals("secure_123", found.get().getPassword());
        assertEquals(Role.ADMIN, found.get().getRole());
    }

    @Test
    void findAll_returnsUsers() {
        // GIVEN two users saved
        u2.setRole(Role.ADMIN);
        u1.setRole(Role.USER);
        userRepository.save(u1);
        userRepository.save(u2);

        // WHEN fetching all users
        List<User> users = userRepository.findAll();

        // THEN both users are returned
        assertThat(users).hasSize(2);
        assertThat(users.get(0).getUsername()).isIn("john","Yann");
    }

    @Test
    void findAll_whenEmptyList() {
        // WHEN the database is empty
        List<User> users = userRepository.findAll();

        // THEN the returned list is empty
        assertThat(users).isEmpty();
    }

    @Test
    void findByEmail_shouldReturnUserWhenExists() {
        // GIVEN a user saved with a specific email
        u1.setRole(Role.USER);
        userRepository.save(u1);

        // WHEN searching by email
        Optional<User> found = userRepository.findByEmail("john@free.fr");

        // THEN the user is found and fields match
        assertThat(found).isPresent();
        assertThat(found.get().getPassword()).isEqualTo("secure_123");
    }

    @Test
    void findByEmail_shouldReturnUserWhenNotExists() {
        // WHEN searching for an email not in the database
        Optional<User> found = userRepository.findByEmail("franck@sfr.fr");

        // THEN the repository returns empty
        assertThat(found).isNotPresent();
    }
}
