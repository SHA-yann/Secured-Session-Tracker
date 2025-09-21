package com.tm.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tm.Exceptions.UserAlreadyExistsException;
import com.tm.Exceptions.UserNotFoundException;

import lombok.Getter;
import lombok.Setter;

/**
 * Global exception handler for REST controllers.
 * Handles specific exceptions and provides structured error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles UserNotFoundException and returns 404 status.
     *
     * @param ex the exception
     * @return structured error response
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles UserAlreadyExistsException and returns 409 status.
     *
     * @param ex the exception
     * @return structured error response
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * Handles generic exceptions and returns 500 status.
     *
     * @param ex the exception
     * @return structured error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * DTO for structured error responses.
     */
    @Getter
    @Setter
    public static class ErrorResponse {

        /** HTTP status code */
        private int status;

        /** Error message */
        private String message;

        public ErrorResponse(int status, String message) {
            this.status = status;
            this.message = message;
        }
    }
}
