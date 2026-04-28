package com.um.dto;

import com.um.model.Role;
import com.um.model.Status;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateRequest(
		
        @Schema(description = "Email address of the user", example = "yann@example.com")
        String email,

        @Schema(description = "Role assigned to the user", example = "ADMIN")
        Role role,

        @Schema(description = "Status of the user account", example = "ACTIVE")
        Status status
) {

}
