package com.um;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Hooks;

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
    
    @PostConstruct
    public void init() {
    	Hooks.enableAutomaticContextPropagation();
    }
}
