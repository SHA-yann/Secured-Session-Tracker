package com.um.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.um.model.Role;
import com.um.model.Status;
import com.um.model.User;

@DataJpaTest                 // Configure un contexte réduit avec une base en mémoire pour tester uniquement la couche repository
@ActiveProfiles("test")       // Utilisation du profil "test" (H2, config simplifiée, etc.)
class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository; // Repository testé

    private User u1;
    private User u2;

    @BeforeEach
    void setup() {
        // Prépare deux utilisateurs avant chaque test (non persistés tant que save() n'est pas appelé)
        u2 = new User("Yann","secure_123","yannsteve@ymail.fr",Role.ADMIN,Status.ACTIVE);
        u1 = new User("John","secure_123","john@free.fr",Role.USER,Status.ACTIVE);

        // Nettoie entièrement la base avant chaque test pour garantir l’isolation des scénarios
        userRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindUser() {
        // GIVEN : un utilisateur correctement initialisé et enrichi
        u2.setCreatedBy("John");
        u2.setUpdatedBy("Yann");

        // WHEN : on enregistre l'utilisateur
        User saved = userRepository.save(u2);

        // THEN : on doit pouvoir le récupérer et vérifier que les valeurs correspondent
        Optional<User> found = userRepository.findById(saved.getId());
        assertNotNull(found);
        assertEquals("Yann", found.get().getUpdatedBy());
        assertEquals(Status.ACTIVE, found.get().getStatus());
    }

    @Test
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
        assertThat(users.get(0).getUsername()).isIn("John","Yann");
        assertThat(users.get(1).getUpdatedBy()).isIn("John","Yann");
    }

    @Test
    void findAll_whenEmptyList() {
        // WHEN : la base est vide (deleteAll exécuté dans setup)
        List<User> users = userRepository.findAll();

        // THEN : la liste retournée doit être vide
        assertThat(users).isEmpty();
    }

    @Test
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
    void findByEmail_shouldReturnUserWhenNotExists() {
        // WHEN : on recherche une adresse qui n'existe pas
        Optional<User> found = userRepository.findByEmail("franck@sfr.fr");

        // THEN : la méthode retourne un Optional vide
        assertThat(found).isNotPresent();
    }
}
