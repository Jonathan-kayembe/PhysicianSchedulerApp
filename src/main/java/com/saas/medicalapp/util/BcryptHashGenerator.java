package com.saas.medicalapp.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt hashes for existing passwords
 * Run this as a standalone Java application to generate hashes for migration
 */
public class BcryptHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Test passwords from database_complete_schema.sql
        System.out.println("-- BCrypt Hashes for existing users");
        System.out.println("-- Generated with BCrypt strength 10");
        System.out.println();
        
        // Physicians
        System.out.println("-- Physicians (role_id = 1)");
        System.out.println("UPDATE users SET password = '" + encoder.encode("lebron2024") + "' WHERE email = 'lebron.james@medical.com';");
        System.out.println("UPDATE users SET password = '" + encoder.encode("jordan23") + "' WHERE email = 'michael.jordan@medical.com';");
        System.out.println("UPDATE users SET password = '" + encoder.encode("kobe24") + "' WHERE email = 'kobe.bryant@medical.com';");
        System.out.println("UPDATE users SET password = '" + encoder.encode("messi10") + "' WHERE email = 'lionel.messi@medical.com';");
        System.out.println("UPDATE users SET password = '" + encoder.encode("ronaldo7") + "' WHERE email = 'cristiano.ronaldo@medical.com';");
        System.out.println("UPDATE users SET password = '" + encoder.encode("curry30") + "' WHERE email = 'stephen.curry@medical.com';");
        System.out.println();
        
        // Nurses
        System.out.println("-- Nurses (role_id = 2)");
        System.out.println("UPDATE users SET password = '" + encoder.encode("mbappe10") + "' WHERE email = 'kylian.mbappe@medical.com';");
        System.out.println("UPDATE users SET password = '" + encoder.encode("haaland9") + "' WHERE email = 'erling.haaland@medical.com';");
        System.out.println("UPDATE users SET password = '" + encoder.encode("giannis34") + "' WHERE email = 'giannis.antetokounmpo@medical.com';");
        System.out.println("UPDATE users SET password = '" + encoder.encode("doncic77") + "' WHERE email = 'luka.doncic@medical.com';");
        System.out.println("UPDATE users SET password = '" + encoder.encode("benzema9") + "' WHERE email = 'karim.benzema@medical.com';");
        System.out.println();
        
        // Staff
        System.out.println("-- Staff (role_id = 3)");
        System.out.println("UPDATE users SET password = '" + encoder.encode("pogba6") + "' WHERE email = 'paul.pogba@medical.com';");
        System.out.println("UPDATE users SET password = '" + encoder.encode("davis3") + "' WHERE email = 'anthony.davis@medical.com';");
        System.out.println("UPDATE users SET password = '" + encoder.encode("vandijk4") + "' WHERE email = 'virgil.vandijk@medical.com';");
        System.out.println("UPDATE users SET password = '" + encoder.encode("westbrook0") + "' WHERE email = 'russell.westbrook@medical.com';");
        System.out.println("UPDATE users SET password = '" + encoder.encode("mane10") + "' WHERE email = 'sadio.mane@medical.com';");
        System.out.println();
        
        // Managers
        System.out.println("-- Managers (role_id = 4)");
        System.out.println("UPDATE users SET password = '" + encoder.encode("zlatan10") + "' WHERE email = 'zlatan.ibrahimovic@medical.com';");
        System.out.println("UPDATE users SET password = '" + encoder.encode("magic32") + "' WHERE email = 'magic.johnson@medical.com';");
        System.out.println("UPDATE users SET password = '" + encoder.encode("ramos4") + "' WHERE email = 'sergio.ramos@medical.com';");
        System.out.println("UPDATE users SET password = '" + encoder.encode("bird33") + "' WHERE email = 'larry.bird@medical.com';");
        System.out.println("UPDATE users SET password = '" + encoder.encode("neuer1") + "' WHERE email = 'manuel.neuer@medical.com';");
        System.out.println();
        
        // SuperAdmin
        System.out.println("-- SuperAdmin (role_id = 5)");
        System.out.println("UPDATE users SET password = '" + encoder.encode("admin123") + "' WHERE email = 'admin@medical.com';");
    }
}

