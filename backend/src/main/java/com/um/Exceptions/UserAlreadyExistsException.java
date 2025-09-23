package com.um.Exceptions;

/**
 * Exception thrown when an attempt is made to create a user
 * that already exists in the system.
 */
public class UserAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message descriptive message explaining the reason for the exception
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
