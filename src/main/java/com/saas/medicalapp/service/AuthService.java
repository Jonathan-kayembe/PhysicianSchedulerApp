package com.saas.medicalapp.service;

import com.saas.medicalapp.model.Role;
import com.saas.medicalapp.model.User;
import com.saas.medicalapp.repository.RoleRepository;
import com.saas.medicalapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
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
        user.setIsActive(true); // Set user as active by default
        
        // Save user to database with error handling
        try {
            User savedUser = userRepository.save(user);
            userRepository.flush(); // Ensure immediate persistence
            return savedUser;
        } catch (DataAccessException e) {
            // Handle database errors (e.g., duplicate email, constraint violations)
            throw new RuntimeException("Database error during registration: " + e.getMessage(), e);
        }
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
        Optional<User> userOptional;
        try {
            userOptional = userRepository.findByEmailWithRole(email);
        } catch (DataAccessException e) {
            // Handle database connection errors
            throw new RuntimeException("Database error during login: " + e.getMessage(), e);
        }
        
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }
        User user = userOptional.get();
        
        // Check if user is active (only if the field exists and is set)
        if (user.getIsActive() != null && !user.getIsActive()) {
            throw new RuntimeException("User account is inactive");
        }
        
        // Verify password
        // Si le mot de passe est haché (BCrypt), utiliser passwordEncoder.matches()
        // Sinon, comparer directement (pour migration des anciens mots de passe en clair)
        String storedPassword = user.getPassword();
        if (storedPassword == null) {
            throw new RuntimeException("Invalid email or password");
        }
        
        boolean passwordMatches = false;
        
        // Vérifier si le mot de passe stocké est un hash BCrypt
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            // Mot de passe haché avec BCrypt, utiliser passwordEncoder.matches()
            passwordMatches = passwordEncoder.matches(password, storedPassword);
        } else {
            // Mot de passe en clair (anciens utilisateurs), comparer directement
            // Permet la période de migration où certains utilisateurs ont des mots de passe en clair
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
