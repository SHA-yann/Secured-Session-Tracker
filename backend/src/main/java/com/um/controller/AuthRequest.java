package com.um.controller;

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
public class AuthRequest {

    /** Username of the user attempting to authenticate */
    private String username;

    /** Password of the user attempting to authenticate */
    private String password;
}
