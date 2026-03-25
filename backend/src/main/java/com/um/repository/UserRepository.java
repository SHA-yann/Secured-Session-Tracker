package com.um.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.um.model.Role;
import com.um.model.User;

/**
 * Repository interface for User entities.
 * Provides CRUD operations, custom searches, and role-based filtering.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by email.
     *
     * @param email user email
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by username.
     *
     * @param username username
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Searches users by username with case-insensitive partial matching.
     *
     * @param username username fragment to search for
     * @param pageable pagination information
     * @return page of matching users
     */
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    /**
     * Finds users by role.
     *
     * @param role role to filter by
     * @param pageable pagination information
     * @return page of users with the specified role
     */
    Page<User> findByRole(Role role, Pageable pageable);

    /**
     * Checks if a username already exists.
     *
     * @param username username to check
     * @return true if the username exists, false otherwise
     */
    boolean existsByUsername(String username);
}