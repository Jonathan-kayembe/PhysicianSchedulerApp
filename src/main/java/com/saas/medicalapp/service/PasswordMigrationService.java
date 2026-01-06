package com.saas.medicalapp.service;

import com.saas.medicalapp.model.User;
import com.saas.medicalapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Password Migration Service
 * Service to migrate existing plain text passwords to BCrypt hashes
 * 
 * WARNING: This service should be used only once during migration.
 * After migration, all passwords should be hashed with BCrypt.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class PasswordMigrationService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Migrate all plain text passwords to BCrypt hashes
     * This method will:
     * 1. Find all users with plain text passwords (not starting with $2a$, $2b$, or $2y$)
     * 2. Hash each password with BCrypt
     * 3. Update the user in the database
     * 
     * @return Map with migration statistics
     */
    public Map<String, Object> migrateAllPasswords() {
        Map<String, Object> result = new HashMap<>();
        int migrated = 0;
        int skipped = 0;
        int errors = 0;
        List<String> errorMessages = new java.util.ArrayList<>();
        
        try {
            List<User> allUsers = userRepository.findAll();
            result.put("totalUsers", allUsers.size());
            
            for (User user : allUsers) {
                try {
                    String currentPassword = user.getPassword();
                    
                    // Skip if password is already hashed (starts with $2a$, $2b$, or $2y$)
                    if (currentPassword != null && 
                        (currentPassword.startsWith("$2a$") || 
                         currentPassword.startsWith("$2b$") || 
                         currentPassword.startsWith("$2y$"))) {
                        skipped++;
                        continue;
                    }
                    
                    // Hash the plain text password with BCrypt
                    String hashedPassword = passwordEncoder.encode(currentPassword);
                    user.setPassword(hashedPassword);
                    userRepository.save(user);
                    migrated++;
                    
                } catch (Exception e) {
                    errors++;
                    errorMessages.add("Error migrating user ID " + user.getId() + " (" + user.getEmail() + "): " + e.getMessage());
                }
            }
            
            // Flush to ensure all changes are persisted
            userRepository.flush();
            
            result.put("success", true);
            result.put("migrated", migrated);
            result.put("skipped", skipped);
            result.put("errors", errors);
            if (!errorMessages.isEmpty()) {
                result.put("errorMessages", errorMessages);
            }
            result.put("message", "Migration completed: " + migrated + " passwords migrated, " + skipped + " skipped, " + errors + " errors");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Migration failed: " + e.getMessage());
            result.put("migrated", migrated);
            result.put("skipped", skipped);
            result.put("errors", errors);
        }
        
        return result;
    }
    
    /**
     * Migrate a specific user's password by email
     * @param email user's email
     * @param plainTextPassword the plain text password to hash and store
     * @return true if migration successful
     */
    public boolean migrateUserPassword(String email, String plainTextPassword) {
        try {
            User user = userRepository.findByEmailWithRole(email);
            if (user == null) {
                return false;
            }
            
            String hashedPassword = passwordEncoder.encode(plainTextPassword);
            user.setPassword(hashedPassword);
            userRepository.save(user);
            userRepository.flush();
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

