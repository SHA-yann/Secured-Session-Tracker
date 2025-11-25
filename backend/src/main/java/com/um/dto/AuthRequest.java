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

    // Getters et setters si nécessaire
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}