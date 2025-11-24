package com.um.DTOs;

import java.time.Instant;

import com.um.model.Role;
import com.um.model.Status;
import com.um.model.User;

/**
 * Data Transfer Object for returning user information in API responses.
 * Maps entity fields to a simplified, immutable record for clients.
 */
public record UserResponse(
        Long id,
        String username,
        String email,
        Role role,
        Status status,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
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
