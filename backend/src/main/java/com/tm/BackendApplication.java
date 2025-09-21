package com.tm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Spring Boot backend application.
 * Bootstraps the application context and starts the embedded server.
 */
@SpringBootApplication
public class BackendApplication {

    /**
     * Main method to launch the Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
