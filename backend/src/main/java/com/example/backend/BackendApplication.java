package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BackendApplication is the Spring Boot entry point for the calendar
 * application backend.
 * 
 * This class bootstraps the Spring framework and starts the embedded web
 * server.
 * The @SpringBootApplication annotation enables:
 * - Component scanning for controllers, services, and configurations
 * - Auto-configuration of Spring Boot features
 * - Configuration properties binding
 * 
 * The backend provides REST API endpoints for:
 * - User authentication and management
 * - Event CRUD operations
 * - Backup and restore functionality
 * - Recurrent event handling
 * 
 * Default server port: 8080 (configurable in application.properties)
 */
@SpringBootApplication
public class BackendApplication {

    /**
     * Main method to launch the Spring Boot application.
     * 
     * @param args Command line arguments (can be used for Spring Boot properties)
     */
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
