package com.um.dto;

import java.time.Instant;

import com.um.model.Role;
import com.um.model.Status;
import com.um.model.User;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for returning user information in API responses.
 * Maps entity fields to a simplified, immutable record for clients.
 */
@Schema(description = "Response DTO for user data")
public record UserResponse(

        @Schema(description = "Unique identifier of the user", example = "1")
        Long id,

        @Schema(description = "Username of the user", example = "yann")
        String username,

        @Schema(description = "Email address of the user", example = "yann@example.com")
        String email,

        @Schema(description = "Role assigned to the user", example = "ADMIN")
        Role role,

        @Schema(description = "Status of the user account", example = "ACTIVE")
        Status status,

        @Schema(description = "Timestamp when the user was created", example = "2025-11-25T13:45:30Z")
        Instant createdAt,

        @Schema(description = "Username who created this user", example = "admin")
        String createdBy,

        @Schema(description = "Timestamp of last update", example = "2025-11-25T14:20:00Z")
        Instant updatedAt,

        @Schema(description = "Username who last updated this user", example = "admin")
        String updatedBy

) {

    /**
     * Converts a User entity to a UserResponse DTO.
     * Encapsulates only safe and relevant fields for API responses.
     *
     * @param user the User entity to map
     * @return a new UserResponse containing the entity's data
     */
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getCreatedBy(),
                user.getUpdatedAt(),
                user.getUpdatedBy()
        );
    }
}
