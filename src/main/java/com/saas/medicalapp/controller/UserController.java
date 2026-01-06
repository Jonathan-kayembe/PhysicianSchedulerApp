package com.saas.medicalapp.controller;

import com.saas.medicalapp.model.User;
import com.saas.medicalapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User Controller
 * REST endpoints for user management
 */
@RestController
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        System.out.println("GET /users - Returning " + users.size() + " users");
        for (User user : users) {
            System.out.println("  - User ID: " + user.getId() + ", Email: " + user.getEmail() + ", Name: " + user.getFullName());
        }
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Update user profile
     * PUT /users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String fullName = request.containsKey("fullName") ? (String) request.get("fullName") : null;
            String email = request.containsKey("email") ? (String) request.get("email") : null;
            
            User updatedUser = userService.updateUser(id, fullName, email);
            
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            
            // Build user info (without password)
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", updatedUser.getId());
            userInfo.put("fullName", updatedUser.getFullName());
            userInfo.put("email", updatedUser.getEmail());
            if (updatedUser.getRole() != null) {
                Map<String, Object> roleInfo = new HashMap<>();
                roleInfo.put("id", updatedUser.getRole().getId());
                roleInfo.put("name", updatedUser.getRole().getName());
                userInfo.put("role", roleInfo);
            }
            response.put("user", userInfo);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

