package com.tm.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.tm.model.Role;
import com.tm.model.User;
import com.tm.service.UserService;

/**
 * REST controller for managing User entities.
 * Provides endpoints for CRUD operations, search, and registration.
 */
@RestController
@RequestMapping("/")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registers a new user.
     *
     * @param user the user to register
     * @return ResponseEntity with location header and created user DTO
     */
    @PostMapping("/users")
    public ResponseEntity<?> register(@RequestBody User user) {

        UserToAdmin created = UserToAdmin.fromEntity(userService.createUser(user));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

            return ResponseEntity.created(location).body(created);
        
    }

    /**
     * Retrieves all users with pagination.
     * Admin-only access.
     *
     * @param pageable pagination info
     * @return page of UserToAdmin DTOs
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserToAdmin>> getAllUsers(@PageableDefault(size = 10, sort = "username") Pageable pageable) {
        Page<User> users = userService.getAllUsers(pageable);
        Page<UserToAdmin> dtoPage = users.map(UserToAdmin::fromEntity);
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Retrieves a user by ID.
     * Admin or the user themselves.
     *
     * @param id user ID
     * @return UserToAdmin DTO or 404
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id==principal.id")
    public ResponseEntity<UserToAdmin> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(UserToAdmin::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a user by email.
     * Admin or the user themselves.
     *
     * @param email user email
     * @return UserToAdmin DTO or 404
     */
    @GetMapping("/users/mail/{email}")
    @PreAuthorize("hasRole('ADMIN') or #id==principal.id")
    public ResponseEntity<UserToAdmin> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(UserToAdmin::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates a user.
     * Admin or the user themselves.
     *
     * @param id   user ID
     * @param user updated user data
     * @return updated UserToAdmin DTO or 404
     */
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id==principal.id")
    public ResponseEntity<UserToAdmin> updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user)
                .map(UserToAdmin::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a user.
     * Admin-only access.
     *
     * @param id user ID
     * @return 204 No Content
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Searches users by username or role with pagination.
     * Admin-only access.
     *
     * @param username optional username fragment
     * @param role     optional role filter
     * @param pageable pagination info
     * @return page of UserToAdmin DTOs
     */
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserToAdmin>> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Role role,
            Pageable pageable) {

        Page<User> users = userService.searchUsers(username, role, pageable);
        Page<UserToAdmin> dtoPage = users.map(UserToAdmin::fromEntity);

        return ResponseEntity.ok(dtoPage);
    }
}
