package com.saas.medicalapp.service;

import com.saas.medicalapp.model.Role;
import com.saas.medicalapp.model.User;
import com.saas.medicalapp.repository.RoleRepository;
import com.saas.medicalapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Authentication Service
 * Handles user registration and login
 */
@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    /**
     * Register a new user
     */
    public User register(String fullName, String email, String password, Integer roleId) {
        // Check if email already exists
        User existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            throw new RuntimeException("Email already exists");
        }
        
        // Validate fields
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        
        // Get role
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        
        // Create new user
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password); // In production, hash the password
        user.setRole(role);
        
        return userRepository.save(user);
    }
    
    /**
     * Login user
     */
    public User login(String email, String password) {
        User user = userRepository.findByEmailAndPassword(email, password);
        if (user == null) {
            throw new RuntimeException("Invalid email or password");
        }
        return user;
    }
    
    /**
     * Logout user (client-side only in this implementation)
     */
    public void logout() {
        // In a real application, you would invalidate the session/token here
    }
}

