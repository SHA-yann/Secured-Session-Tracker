package com.um.model;

/**
 * Represents the activation state of an application entity (e.g., User, Account).
 * <p>
 * This enumeration is commonly used in User Management contexts to differentiate
 * between resources that are currently allowed to operate within the system
 * ({@link #ACTIVE}) and those that are disabled or no longer permitted to interact
 * with the platform ({@link #INACTIVE}).
 * </p>
 *
 * <p><b>OpenAPI usage:</b></p>
 * <ul>
 *   <li><b>ACTIVE</b>: The resource is enabled and fully operational.</li>
 *   <li><b>INACTIVE</b>: The resource is disabled or archived. Access may be restricted.</li>
 * </ul>
 *
 * @apiNote This enum is typically used in request/response DTOs to expose resource state
 *          in API endpoints (e.g., filtering, activation flows, administrative actions).
 */
public enum Status {

    /**
     * The resource is enabled and can operate normally.
     */
    ACTIVE,

    /**
     * The resource is disabled or restricted from performing operations.
     */
    INACTIVE
}
