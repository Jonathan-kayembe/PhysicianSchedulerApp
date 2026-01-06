package com.saas.medicalapp.service;

import com.saas.medicalapp.model.Role;
import com.saas.medicalapp.model.User;
import com.saas.medicalapp.repository.RoleRepository;
import com.saas.medicalapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

/**
 * Authentication Service
 * Handles user registration and login with secure password hashing
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AuthService {
    
    private static final String EMAIL_PATTERN = 
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Register a new user
     * @param fullName user's full name
     * @param email user's email (must be unique and valid format)
     * @param password user's password (will be hashed with BCrypt)
     * @param roleId role ID for the user
     * @return registered User
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException if email already exists or role not found
     */
    public User register(String fullName, String email, String password, Integer roleId) {
        // Validate full name
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (fullName.trim().length() < 2) {
            throw new IllegalArgumentException("Full name must be at least 2 characters");
        }
        
        // Validate email
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        email = email.trim().toLowerCase();
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        
        // Validate password
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        
        // Validate roleId
        if (roleId == null) {
            throw new IllegalArgumentException("Role ID is required");
        }
        
        // Get role
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));
        
        // Hash password with BCrypt before storing
        String hashedPassword = passwordEncoder.encode(password);
        
        // Create new user
        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(email);
        user.setPassword(hashedPassword); // Store hashed password
        user.setRole(role);
        
        // Save user to database
        User savedUser = userRepository.save(user);
        userRepository.flush(); // Ensure immediate persistence
        
        return savedUser;
    }
    
    /**
     * Login user
     * @param email user's email
     * @param password user's password (plain text, will be compared with BCrypt hash)
     * @return User if authentication successful
     * @throws IllegalArgumentException if email or password is null/empty
     * @throws RuntimeException if user not found or password incorrect
     */
    public User login(String email, String password) {
        // Validate email
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        email = email.trim().toLowerCase();
        
        // Validate password
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        
        // Find user by email with role eagerly loaded
        User user = userRepository.findByEmailWithRole(email);
        if (user == null) {
            throw new RuntimeException("Invalid email or password");
        }
        
        // Verify password using BCrypt
        // BCrypt can verify both hashed passwords and plain text passwords (for migration)
        String storedPassword = user.getPassword();
        boolean passwordMatches = false;
        
        // Check if stored password is BCrypt hash (starts with $2a$, $2b$, or $2y$)
        if (storedPassword != null && 
            (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$"))) {
            // Password is hashed with BCrypt, use BCrypt to verify
            passwordMatches = passwordEncoder.matches(password, storedPassword);
        } else {
            // Password is stored in plain text (legacy users), compare directly
            // This allows migration period where some users have plain text passwords
            passwordMatches = password.equals(storedPassword);
        }
        
        if (!passwordMatches) {
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
    
    /**
     * Validate email format
     * @param email email to validate
     * @return true if email format is valid
     */
    private boolean isValidEmail(String email) {
        return pattern.matcher(email).matches();
    }
}
