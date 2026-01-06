package com.saas.medicalapp.config;

import org.springframework.context.annotation.Configuration;

/**
 * CORS Configuration
 * NOTE: This class is disabled to avoid conflicts with SecurityConfig CORS configuration.
 * CORS is handled by SecurityConfig.corsConfigurationSource() which properly configures
 * allowedOrigins without using "*" when allowCredentials is true.
 * 
 * If you need to enable this class, uncomment the code below and remove SecurityConfig CORS.
 */
@Configuration
public class CorsConfig {
    // Disabled - CORS is handled by SecurityConfig to avoid conflicts
    // All CORS configuration is in SecurityConfig.corsConfigurationSource()
}

