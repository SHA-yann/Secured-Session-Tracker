package com.tm.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tm.Exceptions.*;
import com.tm.controller.UserToAdmin;
import com.tm.model.Role;
import com.tm.model.User;
import com.tm.repository.UserRepository;

/**
 * Service for user management.
 * Handles CRUD operations, search, and security access control.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    /**
     * Constructor injecting UserRepository.
     *
     * @param userRepository repository for user entities
     * @param userToAdmin    auxiliary controller (currently unused in constructor)
     */
    public UserService(UserRepository userRepository, UserToAdmin userToAdmin) {
        this.userRepository = userRepository;
    }

    /**
     * Creates a new user with hashed password and default USER role.
     *
     * @param user user to create
     * @return created user
     * @throws UserAlreadyExistsException if username is already taken
     */
    @Transactional
    public User createUser(User user) {
        user.setPassword(new BCryptPasswordEncoder(12).encode(user.getPassword()));
        user.setRole(Role.USER);

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException("Username " + user.getUsername() + " already taken");
        }

        return userRepository.save(user);
    }

    /**
     * Retrieves all users with pagination.
     *
     * @param pageable pagination settings
     * @return page of users
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Retrieves a user by ID.
     *
     * @param id user ID
     * @return Optional containing the user if found
     * @throws UserNotFoundException if user not found
     */
    @PreAuthorize("hasRole('ADMIN') or #id==principal.id")
    public Optional<User> getUserById(long id) {
        return Optional.of(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found")));
    }

    /**
     * Retrieves a user by email.
     *
     * @param email user email
     * @return Optional containing the user if found
     * @throws UserNotFoundException if user not found
     */
    @PreAuthorize("hasRole('ADMIN') or #id==principal.id")
    public Optional<User> getUserByEmail(String email) {
        return Optional.of(userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found")));
    }

    /**
     * Updates a user’s information.
     * Admins can also update roles.
     *
     * @param id     user ID
     * @param update updated user data
     * @return Optional containing updated user
     * @throws UserNotFoundException if user not found
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or #id==principal.id")
    public Optional<User> updateUser(long id, User update) {
        return Optional.of(userRepository.findById(id)
                .map(found -> {
                    found.setUsername(update.getUsername());
                    found.setEmail(update.getEmail());

                    boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                            .getAuthorities()
                            .stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

                    if (isAdmin && update.getRole() != null && !update.getRole().name().isEmpty()) {
                        found.setRole(update.getRole());
                    }

                    return userRepository.save(found);
                })
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found")));
    }

    /**
     * Deletes a user by ID.
     *
     * @param id user ID
     * @throws UserNotFoundException if user not found
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User with id " + id + " not found");
        }
        userRepository.deleteById(id);
    }

    /**
     * Finds a user by username.
     *
     * @param username username
     * @return Optional containing the user
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Optional<User> findByName(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Searches users by username or role with pagination.
     *
     * @param username optional username filter
     * @param role     optional role filter
     * @param pageable pagination settings
     * @return page of users matching filters
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Page<User> searchUsers(String username, Role role, Pageable pageable) {
        if (username != null && !username.isBlank()) {
            return userRepository.findByUsernameContainingIgnoreCase(username, pageable);
        }
        if (role != null) {
            return userRepository.findByRole(role, pageable);
        }
        return userRepository.findAll(pageable);
    }

    /**
     * Deletes all users and flushes the repository.
     */
    public void wipeAll() {
        userRepository.deleteAll();
        userRepository.flush();
    }
}
