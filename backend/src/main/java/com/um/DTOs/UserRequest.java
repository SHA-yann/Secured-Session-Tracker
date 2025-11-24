package com.um.DTOs;

import com.um.model.Role;
import com.um.model.Status;
import com.um.model.User;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for creating or updating a User.
 * Ensures required fields are not blank and provides conversion to User entity.
 */
public record UserRequest(
        @NotBlank
        String username,
        @NotBlank
        String password,
        @NotBlank
        String email,
        @NotBlank
        Role role,
        @NotBlank
        Status status
) {

    /**
     * Converts this DTO to a User entity.
     * Note: The password is set to null here; it should be encoded and set separately.
     *
     * @return User entity with values from this DTO
     */
    public User toEntity() {
        User user = new User();
        user.setUsername(this.username);
        user.setPassword(null); // password will need encoding before persisting
        user.setEmail(this.email);
        user.setRole(this.role);
        user.setStatus(this.status);
        return user;
    }
}
