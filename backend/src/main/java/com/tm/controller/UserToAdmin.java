package com.tm.controller;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.tm.model.Role;
import com.tm.model.User;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for transferring User data to admin views.
 * Includes mapping methods to convert to/from User entity.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Component
public class UserToAdmin {

    /** User ID */
    private Long Id;

    /** Username, required */
    @NotBlank
    private String username;

    /** User role, required */
    @NotBlank
    private Role role;

    /** Email, required */
    @NotBlank
    private String email;

    /** Creation timestamp */
    private Instant createdAt;

    /** Last update timestamp */
    private Instant updatedAt;

    /**
     * Converts a User entity to a UserToAdmin DTO.
     *
     * @param user the User entity
     * @return corresponding UserToAdmin DTO
     */
    public static UserToAdmin fromEntity(User user) {
        return new UserToAdmin(
            user.getId(),
            user.getUsername(),
            user.getRole(),
            user.getEmail(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    /**
     * Converts this DTO to a User entity.
     *
     * @return User entity populated from DTO
     */
    public User toEntity() {
        User user = new User();
        user.setId(this.Id);
        user.setUsername(this.username);
        user.setEmail(this.email);
        user.setRole(this.role);
        return user;
    }
}
