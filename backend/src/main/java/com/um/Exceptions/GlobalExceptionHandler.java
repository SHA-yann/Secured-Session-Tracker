package com.um.Exceptions;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.Getter;
import lombok.Setter;
import reactor.core.publisher.Mono;

/**
 * Global exception handler for REST controllers.
 * Handles specific exceptions and provides structured error responses.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    /**
     * Handles UserNotFoundException and returns 404 status.
     *
     * @param ex the exception
     * @return structured error response
     */
    @ExceptionHandler(UserNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
        		"No user found with these informations "+ex.getMessage());
        return Mono.just(new ResponseEntity<>(error, HttpStatus.NOT_FOUND));
    }

    /**
     * Handles UserAlreadyExistsException and returns 409 status.
     *
     * @param ex the exception
     * @return structured error response
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(),
        		"The user already exist "+ex.getMessage());
        return Mono.just(new ResponseEntity<>(error, HttpStatus.CONFLICT));
    }

    /**
     * Handles IllegalArgumentException and returns 500 status.
     *
     * @param ex the exception
     * @return structured error response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegal(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                "An argument is not correct "+ex.getMessage());
        return Mono.just(new ResponseEntity<>(error, HttpStatus.BAD_REQUEST));
    }

    /**
     * Handles NullPointerException and returns 500 status.
     *
     * @param ex the exception
     * @return structured error response
     */
    @ExceptionHandler(NullPointerException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleNull(NullPointerException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                "cannot reference a null object "+ex.getMessage());
        return Mono.just(new ResponseEntity<>(error, HttpStatus.BAD_REQUEST));
    }
    
    /**
     * Handles BadCredentialsException and returns 401 status.
     *
     * @param ex the exception
     * @return structured error response
     */
    @ExceptionHandler(BadCredentialsException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleNull(BadCredentialsException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                "You are not authenticated "+ex.getMessage());
        return Mono.just(new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED));
    }
    
    /**
     * Handles AccessDeniedException and returns 403 status.
     *
     * @param ex the exception
     * @return structured error response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleNull(AccessDeniedException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(),
             "Access denied for user "+SecurityContextHolder.getContext().getAuthentication().getName()
             +", check details"+ex.getMessage());
        return Mono.just(new ResponseEntity<>(error, HttpStatus.FORBIDDEN));
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleNotFound(ResourceNotFoundException ex){
    	
    	ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
                "Error "+ex.getMessage());
           return Mono.just(new ResponseEntity<>(error, HttpStatus.NOT_FOUND));
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
