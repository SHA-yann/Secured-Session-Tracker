package com.um.controller;

import java.net.URI;
import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import com.um.dto.UpdateRequest;
import com.um.dto.UserRequest;
import com.um.dto.UserResponse;
import com.um.model.Role;
import com.um.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
    public Mono<ResponseEntity<UserResponse>> register(
            
            @RequestBody UserRequest user) {

        return ReactiveSecurityContextHolder.getContext()
        		.map(SecurityContext::getAuthentication)
        		.map(Principal::getName)
        		.flatMap(author -> userService.createUser(user, author))
        		.map(created -> {
					URI location = UriComponentsBuilder.fromPath("/{id}")
											                .buildAndExpand(created.getId())
											                .toUri();
					return ResponseEntity.created(location).body(UserResponse.fromEntity(created));
	        	});
        
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
    public Mono<ResponseEntity<PagedModel<UserResponse>>> getAllUsers(
            @Parameter(description = "Pagination information") 
             Pageable pageable) {

        return Mono.defer(() -> userService.getAllUsers(pageable))
							.map(usersPage -> {
								Page<UserResponse> dtoPage = usersPage.map(UserResponse::fromEntity);
								PagedModel<UserResponse> pMod = new PagedModel<>(dtoPage);
								return pMod;
								})
							.flatMap(pMod -> Mono.just(ResponseEntity.ok(pMod)))
							.switchIfEmpty(Mono.just(ResponseEntity.noContent().build()));
        
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
    public Mono<ResponseEntity<UserResponse>> getUserById(
            @Parameter(description = "ID of the user to retrieve", required = true)
            @PathVariable Long id) {

        return userService.getUserById(id)
                .map(user ->{ UserResponse dto = UserResponse.fromEntity(user);
                	return ResponseEntity.ok(dto);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
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
    public Mono<ResponseEntity<UserResponse>> getUserByEmail(
            @Parameter(description = "Email of the user to retrieve", required = true)
            @PathVariable String email) {

    	return userService.getUserByEmail(email)
                .map(user ->{ UserResponse dto = UserResponse.fromEntity(user);
                	return ResponseEntity.ok(dto);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
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
    @PutMapping("/users/{id}")
    public Mono<ResponseEntity<UserResponse>> updateUser(
            @Parameter(description = "id of the user to update", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Updated user data", required = true
            )
            @RequestBody UpdateRequest update) {

        return ReactiveSecurityContextHolder.getContext()
        		.map(ctx -> ctx.getAuthentication().getName())
        		.flatMap(author -> 
        				userService.updateUser(id, update, author)
                        .map(UserResponse::fromEntity)
                        .map(ResponseEntity::ok))
        				.defaultIfEmpty(ResponseEntity.notFound().build());
        		
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
    public Mono<ResponseEntity<Void>> disableUser(
            @Parameter(description = "ID of the user to disable", required = true)
            @PathVariable Long id) {

        return userService.disableUser(id)
        				  .thenReturn(ResponseEntity.noContent().build());
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
    public Mono<ResponseEntity<PagedModel<UserResponse>>> searchUsers(
            @Parameter(description = "Username filter", required = false) @RequestParam(required = false) String username,
            @Parameter(description = "Role filter", required = false) @RequestParam(required = false) Role role,
            @Parameter(description = "Pagination information") Pageable pageable) {

        return userService.searchUsers(username, role, pageable)
        					.map(usersPage -> {
    							Page<UserResponse> dtoPage = usersPage.map(UserResponse::fromEntity);
    							PagedModel<UserResponse> pMod = new PagedModel<>(dtoPage);
    							return ResponseEntity.ok(pMod);
    						});

    }
}

