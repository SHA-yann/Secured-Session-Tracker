package com.um.model;

/**
 * Defines the set of authorization roles available within the User Management platform.
 * <p>
 * Roles determine the level of access and the scope of operations a user is permitted
 * to perform. They are typically consumed by Spring Security for access-control
 * decisions and exposed in API responses for auditability and administrative workflows.
 * </p>
 *
 * <p><b>OpenAPI usage:</b></p>
 * <ul>
 *   <li><strong>ADMIN</strong>: Full administrative privileges. Can manage users,
 *       roles, and configuration-level operations.</li>
 *   <li><strong>USER</strong>: Standard account with restricted access, typically
 *       limited to self-service operations.</li>
 * </ul>
 *
 * @apiNote These values are commonly mapped to {@code GrantedAuthority} objects
 *          when integrating with Spring Security.
 */
public enum Role {

    /**
     * Full-access administrative role capable of performing privileged operations.
     * Intended for system administrators and platform managers.
     */
    ADMIN,

    /**
     * Standard user role with limited operational rights.
     * Suitable for regular application users.
     */
    USER
}
