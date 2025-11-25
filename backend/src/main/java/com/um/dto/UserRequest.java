package com.um.dto;

import com.um.model.Role;
import com.um.model.Status;
import com.um.model.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for creating or updating a User.
 * Ensures required fields are not blank and provides conversion to User entity.
 */
@Schema(description = "Request DTO for creating or updating a user")
public record UserRequest(

        @NotBlank
        @Schema(description = "Username of the user", example = "yann")
        String username,

        @NotBlank
        @Schema(description = "Password of the user", example = "P@ssw0rd123")
        String password,

        @NotBlank
        @Schema(description = "Email address of the user", example = "yann@example.com")
        String email,

        @NotBlank
        @Schema(description = "Role assigned to the user", example = "ADMIN")
        Role role,

        @NotBlank
        @Schema(description = "Status of the user account", example = "ACTIVE")
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
