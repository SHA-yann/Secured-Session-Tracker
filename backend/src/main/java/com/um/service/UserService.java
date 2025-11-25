package com.um.service;

import java.util.Optional;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.um.Exceptions.*;
import com.um.dto.UserRequest;
import com.um.model.Role;
import com.um.model.User;
import com.um.repository.UserRepository;

/**
 * Service for user management.
 * Handles CRUD operations, search, and security access control.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a UserService with required dependencies.
     *
     * @param userRepository repository for accessing user entities
     * @param passwordEncoder encoder for hashing user passwords
     */
    public UserService(UserRepository userRepository,@Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new user with hashed password and default USER role.
     *
     * @param user user to create
     * @return created user
     * @throws UserAlreadyExistsException if username is already taken
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public User createUser(UserRequest dto, String author) {
        
    	if (userRepository.existsByUsername(dto.username())) {
            throw new UserAlreadyExistsException("Username " + dto.username() + " already taken");
        }
    	
    	User user= new User(
    	dto.username(),
    	passwordEncoder.encode(dto.password()),
    	dto.email(),
    	dto.role(),
        dto.status());
        user.setCreatedBy(author);
        user.setUpdatedBy(author);

        return userRepository.save(user);
    }

    /**
     * Retrieves all users with pagination.
     *
     * @param pageable pagination settings
     * @return page of users
     */
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN') or #username==principal.username")
    public Optional<User> updateUser(String username, UserRequest update, String author) {
        return Optional.of(userRepository.findByUsername(username)
                .map(found -> {
                    found.setEmail(update.email());
                    found.setCreatedBy(author);
                    found.setUpdatedBy(author);

                    boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                            .getAuthorities()
                            .stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

                    if (isAdmin) {
                        found.setRole(update.role());
                        found.setStatus(update.status());
                    }

                    return userRepository.save(found);
                })
                .orElseThrow(() -> new UserNotFoundException("User with name " + username + " not found")));
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
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
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
