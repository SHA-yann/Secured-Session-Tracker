package com.um;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Hooks;

/**
 * Main entry point for the Spring Boot backend application.
 * Bootstraps the application context and starts the embedded server.
 */
@EnableScheduling
@SpringBootApplication
public class BackendApplication {

    /**
     * Main method to launch the Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
    	    	
        SpringApplication.run(BackendApplication.class, args);
        //String mdp = new BCryptPasswordEncoder(12).encode("Password123!");
        //System.out.println("le Hash est:"+mdp);
    }
    
    @PostConstruct
    public void init() {
    	Hooks.enableAutomaticContextPropagation();
    }
}
