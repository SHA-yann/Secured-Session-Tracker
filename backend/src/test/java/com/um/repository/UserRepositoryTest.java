package com.um.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.um.BackendApplication;
import com.um.configuration.AbstractIntegrationTest;
import com.um.model.Role;
import com.um.model.Status;
import com.um.model.User;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = BackendApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest extends AbstractIntegrationTest {
    
    @Autowired
    private UserRepository userRepository;

    private User u1;
    private User u2;

    @BeforeEach
    void setup() {
        // Nettoie entièrement la base avant chaque test pour garantir l’isolation des scénarios
        userRepository.deleteAll();

        // Prépare deux utilisateurs avant chaque test (non persistés tant que save() n'est pas appelé)
        u2 = new User("Yann", "secure_123", "yannsteve@ymail.fr", Role.ADMIN, Status.ACTIVE);
        u1 = new User("John", "secure_123", "john@free.fr", Role.USER, Status.ACTIVE);
    }

    @Test
    @DisplayName("Should successfully persist and retrieve a user with lifecycle fields")
    void shouldSaveAndFindUser() {
        // GIVEN : un utilisateur correctement initialisé et enrichi
        u2.setCreatedBy("John");
        u2.setUpdatedBy("Yann");

        // WHEN : on enregistre l'utilisateur
        User saved = userRepository.save(u2);

        // THEN : on doit pouvoir le récupérer et vérifier que les valeurs correspondent
        Optional<User> found = userRepository.findById(saved.getId());
        
        assertThat(found).isPresent();
        assertThat(found.get().getUpdatedBy()).isEqualTo("Yann");
        assertThat(found.get().getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(found.get().getCreatedAt()).isNotNull(); // Valide l'effet du @PrePersist
    }

    @Test
    @DisplayName("Should return a list containing all registered users")
    void findAll_returnsUsers() {
        // GIVEN : deux utilisateurs enregistrés dans la base
        u2.setStatus(Status.INACTIVE);
        u1.setStatus(Status.ACTIVE);
        u2.setCreatedBy("John");
        u2.setUpdatedBy("Yann");
        u1.setCreatedBy("John");
        u1.setUpdatedBy("Yann");
        userRepository.save(u1);
        userRepository.save(u2);

        // WHEN : on récupère tous les utilisateurs
        List<User> users = userRepository.findAll();

        // THEN : la liste contient les deux entrées et les valeurs sont cohérentes
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getUsername).containsExactlyInAnyOrder("John", "Yann");
    }

    @Test
    @DisplayName("Should return an empty list when no users exist")
    void findAll_whenEmptyList() {
        // WHEN : la base est vide
        List<User> users = userRepository.findAll();

        // THEN : la liste retournée doit être vide
        assertThat(users).isEmpty();
    }

    @Test
    @DisplayName("Should find a single user when matching email exists")
    void findByEmail_shouldReturnUserWhenExists() {
        // GIVEN : un utilisateur enregistré avec une adresse email spécifique
        u1.setStatus(Status.ACTIVE);
        u1.setCreatedBy("John");
        u1.setUpdatedBy("Yann");
        userRepository.save(u1);

        // WHEN : on recherche cet email dans la base
        Optional<User> found = userRepository.findByEmail("john@free.fr");

        // THEN : l’utilisateur est retrouvé et les champs sont corrects
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("John");
        assertThat(found.get().getUpdatedBy()).isEqualTo("Yann");
    }

    @Test
    @DisplayName("Should return empty optional when searching a non-existent email")
    void findByEmail_shouldReturnEmptyWhenNotExists() {
        // WHEN : on recherche une adresse qui n'existe pas
        Optional<User> found = userRepository.findByEmail("franck@sfr.fr");

        // THEN : la méthode retourne un Optional vide
        assertThat(found).isNotPresent();
    }

    @Test
    @DisplayName("Should return a case-insensitive paginated match on username fragment")
    void shouldFindByUsernameContainingIgnoreCase() {
        // GIVEN
        u1.setCreatedBy("System"); u1.setUpdatedBy("System");
        u2.setCreatedBy("System"); u2.setUpdatedBy("System");
        userRepository.save(u1); // John
        userRepository.save(u2); // Yann

        Pageable pageable = PageRequest.of(0, 10);

        // WHEN : On cherche avec un fragment mixte "oH" pour cibler "John"
        Page<User> result = userRepository.findByUsernameContainingIgnoreCase("oH", pageable);

        // THEN
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("John");
    }

    @Test
    @DisplayName("Should filter and paginate users strictly by their Role")
    void shouldFindByRole() {
        // GIVEN
        u1.setCreatedBy("System"); u1.setUpdatedBy("System");
        u2.setCreatedBy("System"); u2.setUpdatedBy("System");
        userRepository.save(u1); // Role.USER
        userRepository.save(u2); // Role.ADMIN

        Pageable pageable = PageRequest.of(0, 10);

        // WHEN
        Page<User> adminPage = userRepository.findByRole(Role.ADMIN, pageable);

        // THEN
        assertThat(adminPage.getContent()).hasSize(1);
        assertThat(adminPage.getContent().get(0).getUsername()).isEqualTo("Yann");
    }

    @Test
    @DisplayName("Should accurately check the presence of a username")
    void shouldVerifyIfUsernameExists() {
        // GIVEN
        u2.setCreatedBy("System"); u2.setUpdatedBy("System");
        userRepository.save(u2);

        // WHEN
        boolean exists = userRepository.existsByUsername("Yann");
        boolean doesNotExist = userRepository.existsByUsername("Unknown");

        // THEN
        assertThat(exists).isTrue();
        assertThat(doesNotExist).isFalse();
    }
}

