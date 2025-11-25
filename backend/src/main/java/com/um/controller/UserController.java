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

import com.um.dto.UserRequest;
import com.um.dto.UserResponse;
import com.um.model.Role;
import com.um.model.User;
import com.um.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
        summary = "Register a new user",
        description = "Creates a new user in the system. Only accessible by ADMIN.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Forbidden, insufficient permissions")
    })
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "User data to create", required = true
            )
            @RequestBody UserRequest user) {

        String author = SecurityContextHolder.getContext().getAuthentication().getName();
        User created = userService.createUser(user, author);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(UserResponse.fromEntity(created));
    }

    @Operation(
        summary = "Get all users",
        description = "Retrieves all users with pagination. Accessible by ADMIN and USER.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden, insufficient permissions")
    })
    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @Parameter(description = "Pagination information") 
            @PageableDefault(size = 10, sort = "username") Pageable pageable) {

        Page<User> users = userService.getAllUsers(pageable);
        Page<UserResponse> dtoPage = users.map(UserResponse::fromEntity);
        return ResponseEntity.ok(dtoPage);
    }

    @Operation(
        summary = "Get user by ID",
        description = "Retrieves a user by their ID. Accessible by ADMIN only.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "ID of the user to retrieve", required = true)
            @PathVariable Long id) {

        return userService.getUserById(id)
                .map(UserResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Get user by email",
        description = "Retrieves a user by their email address. Accessible by ADMIN only.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/users/mail/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserByEmail(
            @Parameter(description = "Email of the user to retrieve", required = true)
            @PathVariable String email) {

        return userService.getUserByEmail(email)
                .map(UserResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Update a user",
        description = "Updates a user's data. Accessible by ADMIN or the user themselves.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User successfully updated"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PutMapping("/users/{username}")
    @PreAuthorize("hasRole('ADMIN') or #username==principal.username")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "Username of the user to update", required = true)
            @PathVariable String username,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Updated user data", required = true
            )
            @RequestBody UserRequest user) {

        String author = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.updateUser(username, user, author)
                .map(UserResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Delete a user",
        description = "Deletes a user by ID. Accessible by ADMIN only.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to delete", required = true)
            @PathVariable Long id) {

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Search users",
        description = "Searches users by username or role with pagination. Accessible by ADMIN and USER.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/users/search")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @Parameter(description = "Username filter", required = false) @RequestParam(required = false) String username,
            @Parameter(description = "Role filter", required = false) @RequestParam(required = false) Role role,
            @Parameter(description = "Pagination information") Pageable pageable) {

        Page<User> users = userService.searchUsers(username, role, pageable);
        Page<UserResponse> dtoPage = users.map(UserResponse::fromEntity);
        return ResponseEntity.ok(dtoPage);
    }
}
