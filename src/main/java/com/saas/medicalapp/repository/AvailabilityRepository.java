package com.saas.medicalapp.repository;

import com.saas.medicalapp.model.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Availability Repository
 * JPA repository for Availability entity
 */
@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Integer> {
    List<Availability> findByUserId(Integer userId);
    
    @Query("SELECT a FROM Availability a WHERE a.user.id = :userId AND DATE(a.startTime) = DATE(:date)")
    List<Availability> findByUserIdAndDate(@Param("userId") Integer userId, @Param("date") LocalDateTime date);
}

