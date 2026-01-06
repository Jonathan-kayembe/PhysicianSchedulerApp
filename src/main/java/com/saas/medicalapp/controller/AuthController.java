package com.saas.medicalapp.controller;

import com.saas.medicalapp.model.User;
import com.saas.medicalapp.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller
 * Handles registration, login, and logout endpoints
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * User registration endpoint
     * POST /auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract and validate request parameters
            String fullName = extractString(request, "fullName");
            String email = extractString(request, "email");
            String password = extractString(request, "password");
            Integer roleId = extractInteger(request, "roleId");
            
            System.out.println("Registration request received:");
            System.out.println("  Full Name: " + fullName);
            System.out.println("  Email: " + email);
            System.out.println("  Role ID: " + roleId);
            
            // Register user
            User user = authService.register(fullName, email, password, roleId);
            
            System.out.println("Registration successful for user ID: " + user.getId());
            
            // Build success response
            response.put("success", true);
            response.put("message", "Registration successful");
            
            // Build user info (without password) - format expected by frontend
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("fullName", user.getFullName());
            userInfo.put("email", user.getEmail());
            if (user.getRole() != null) {
                Map<String, Object> roleInfo = new HashMap<>();
                roleInfo.put("id", user.getRole().getId());
                roleInfo.put("name", user.getRole().getName());
                userInfo.put("role", roleInfo);
            }
            response.put("user", userInfo);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            // Validation errors
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (RuntimeException e) {
            // Business logic errors (duplicate email, role not found, etc.)
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            // Unexpected errors
            response.put("success", false);
            response.put("message", "An error occurred during registration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * User login endpoint
     * POST /auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract and validate request parameters
            String email = extractString(request, "email");
            String password = extractString(request, "password");
            
            // Login user
            User user = authService.login(email, password);
            
            // Build success response
            response.put("success", true);
            response.put("message", "Login successful");
            
            // Build user info (without password) - format expected by frontend
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("fullName", user.getFullName());
            userInfo.put("email", user.getEmail());
            if (user.getRole() != null) {
                Map<String, Object> roleInfo = new HashMap<>();
                roleInfo.put("id", user.getRole().getId());
                roleInfo.put("name", user.getRole().getName());
                userInfo.put("role", roleInfo);
            }
            response.put("user", userInfo);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            // Validation errors
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (RuntimeException e) {
            // Authentication errors (invalid credentials)
            response.put("success", false);
            response.put("message", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            
        } catch (Exception e) {
            // Unexpected errors
            response.put("success", false);
            response.put("message", "An error occurred during login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * User logout endpoint
     * GET /auth/logout
     */
    @GetMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> response = new HashMap<>();
        try {
            authService.logout();
            response.put("success", true);
            response.put("message", "Logout successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred during logout: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Extract string from request map with validation
     */
    private String extractString(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value == null) {
            throw new IllegalArgumentException(key + " is required");
        }
        if (value instanceof String) {
            return (String) value;
        }
        return value.toString();
    }
    
    /**
     * Extract integer from request map with validation
     */
    private Integer extractInteger(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value == null) {
            throw new IllegalArgumentException(key + " is required");
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + " must be a valid number");
        }
    }
    
    /**
     * Password migration endpoint
     * POST /auth/migrate-passwords
     * 
     * WARNING: This endpoint should be disabled in production after migration.
     * It migrates all plain text passwords to BCrypt hashes.
     */
    @PostMapping("/migrate-passwords")
    public ResponseEntity<Map<String, Object>> migratePasswords() {
        Map<String, Object> response = new HashMap<>();
        try {
            // This will be injected
            response.put("success", false);
            response.put("message", "Migration service not yet configured. Use PasswordMigrationController instead.");
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Migration error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

