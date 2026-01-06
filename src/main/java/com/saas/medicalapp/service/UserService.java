package com.saas.medicalapp.service;

import com.saas.medicalapp.model.User;
import com.saas.medicalapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * User Service
 * Business logic for user management
 */
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        System.out.println("UserService.getAllUsers() - Found " + users.size() + " users in database");
        return users;
    }
    
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    /**
     * Update user information
     * @param id user ID
     * @param fullName new full name (optional)
     * @param email new email (optional, must be unique if provided)
     * @return updated User
     */
    public User updateUser(Integer id, String fullName, String email) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update full name if provided
        if (fullName != null && !fullName.trim().isEmpty()) {
            if (fullName.trim().length() < 2) {
                throw new IllegalArgumentException("Full name must be at least 2 characters");
            }
            user.setFullName(fullName.trim());
            System.out.println("Updating full name for user ID " + id + " to: " + fullName.trim());
        }
        
        // Update email if provided
        if (email != null && !email.trim().isEmpty()) {
            email = email.trim().toLowerCase();
            
            // Check if email is different from current
            if (!email.equals(user.getEmail())) {
                // Check if new email already exists
                if (userRepository.existsByEmail(email)) {
                    throw new RuntimeException("Email already exists");
                }
                user.setEmail(email);
                System.out.println("Updating email for user ID " + id + " to: " + email);
            }
        }
        
        User savedUser = userRepository.save(user);
        userRepository.flush();
        System.out.println("User updated successfully: ID " + savedUser.getId());
        
        return savedUser;
    }
}

