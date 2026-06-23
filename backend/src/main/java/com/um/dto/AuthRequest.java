package com.um.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO representing an authentication request.
 * Contains the credentials required for login.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Request object for user authentication")
public class AuthRequest {

    @Schema(description = "Username of the user attempting to authenticate", example = "yann")
    private String username;

    @Schema(description = "Password of the user attempting to authenticate", example = "P@ssw0rd123")
    private String password;

}
