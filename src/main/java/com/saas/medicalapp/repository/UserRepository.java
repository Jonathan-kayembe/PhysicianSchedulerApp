package com.saas.medicalapp.repository;

import com.saas.medicalapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Repository
 * JPA repository for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    /**
     * Find user by email
     * @param email user email
     * @return Optional<User> user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find user by email with role eagerly loaded
     * @param email user email
     * @return Optional<User> with role loaded
     */
    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.email = :email")
    Optional<User> findByEmailWithRole(@Param("email") String email);
    
    /**
     * Check if email exists
     * @param email user email
     * @return true if email exists
     */
    boolean existsByEmail(String email);
}

