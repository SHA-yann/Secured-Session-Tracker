package com.um.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.um.DTOs.UserRequest;
import com.um.DTOs.UserResponse;
import com.um.model.Role;
import com.um.model.User;
import com.um.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller that exposes REST endpoints for managing users.
 * Provides registration, retrieval, update, deletion, and search functionality.
 */
@RestController
@RequestMapping("/")
@Tag(name = "Users", description = "Endpoints for user management")

public class UserController {

    private final UserService userService;

    /**
     * Creates a new {@code UserController}.
     *
     * @param userService service for user management operations
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registers a new user.
     *
     * @param user the user to create
     * @return a response containing the created user and the location URI
     */
    
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> register(@RequestBody UserRequest user) {
    	
    	String author = SecurityContextHolder.getContext().getAuthentication().getName();
        User created = userService.createUser(user,author);
        
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(
        		new UserResponse(
        			created.getId(),
        			created.getUsername(),
        			created.getEmail(),
        			created.getRole(),
        			created.getStatus(),
        			created.getCreatedAt(),
        			created.getCreatedBy(),
        			created.getUpdatedAt(),
        			created.getUpdatedBy()
        				)
        		);
    }

    /**
     * Retrieves all users with pagination.
     * Accessible to administrators only.
     *
     * @param pageable pagination information
     * @return a page of users
     */
    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(security=@SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @PageableDefault(size = 10, sort = "username") Pageable pageable) {
        Page<User> users = userService.getAllUsers(pageable);
        Page<UserResponse> dtoPage = users.map(UserResponse::fromEntity);
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Retrieves a user by ID.
     * Accessible to administrators or the user themselves.
     *
     * @param id the user ID
     * @return the user if found, otherwise 404
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(security=@SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(UserResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a user by email.
     * Accessible to administrators or the user themselves.
     *
     * @param email the user email
     * @return the user if found, otherwise 404
     */
    @GetMapping("/users/mail/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(security=@SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(UserResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates a user.
     * Accessible to administrators or the user themselves.
     *
     * @param id   the user ID
     * @param user the updated user data
     * @return the updated user if found, otherwise 404
     */
    @PutMapping("/users/{username}")
    @PreAuthorize("hasRole('ADMIN') or #username==principal.username")
    @Operation(security=@SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserResponse> updateUser(@PathVariable String username, @RequestBody UserRequest user) {
        
    	String author = SecurityContextHolder.getContext().getAuthentication().getName();
    	return userService.updateUser(username, user,author)
                .map(UserResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a user.
     * Accessible to administrators only.
     *
     * @param id the user ID
     * @return 204 if deleted
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(security=@SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Searches users by username or role with pagination.
     * Accessible to administrators only.
     *
     * @param username optional username filter
     * @param role     optional role filter
     * @param pageable pagination information
     * @return a page of matching users
     */
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(security=@SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Role role,
            Pageable pageable) {

        Page<User> users = userService.searchUsers(username, role, pageable);
        Page<UserResponse> dtoPage = users.map(UserResponse::fromEntity);

        return ResponseEntity.ok(dtoPage);
    }
}
