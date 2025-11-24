package com.um.Exceptions;

/**
 * Exception thrown when a requested user cannot be found in the system.
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message descriptive message explaining the reason for the exception
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
