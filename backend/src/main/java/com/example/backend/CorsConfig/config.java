package com.example.backend.CorsConfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS (Cross-Origin Resource Sharing) configuration for the Spring Boot
 * backend.
 * 
 * This configuration enables the backend API to accept requests from the JavaFX
 * frontend
 * which may be running on a different port or origin.
 * 
 * CORS Configuration:
 * - **Allowed Origins**: localhost:8080 (backend), localhost:3000 (development
 * frontend)
 * - **Allowed Methods**: GET, POST, PUT, DELETE, OPTIONS
 * - **Allowed Headers**: All headers (*)
 * - **Credentials**: Enabled (allows cookies and authentication headers)
 * - **Max Age**: 3600 seconds (1 hour) - browsers cache preflight responses
 * 
 * Security Note: For production, replace wildcard origins with specific
 * frontend URLs.
 * 
 * @see WebMvcConfigurer
 */
@Configuration
public class config implements WebMvcConfigurer {

    /**
     * Configures CORS mappings for all API endpoints.
     * 
     * This method is automatically called by Spring during application startup.
     * 
     * Configuration Details:
     * - **Mapping**: "/**" - Applies to all endpoints
     * - **Allowed Origins**:
     * - http://localhost:8080 (Spring Boot backend)
     * - http://localhost:3000 (Development server)
     * - **Allowed Methods**: Standard REST operations + OPTIONS for preflight
     * - **Allowed Headers**: All (*) - Accepts any request headers
     * - **Allow Credentials**: true - Enables cookie/session sharing
     * - **Max Age**: 3600s - Preflight response cache duration
     * 
     * Preflight Requests:
     * Browsers send OPTIONS requests before actual requests to check permissions.
     * The maxAge setting reduces unnecessary preflight requests.
     * 
     * @param registry Spring's CorsRegistry for adding CORS mappings
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8080", "http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
