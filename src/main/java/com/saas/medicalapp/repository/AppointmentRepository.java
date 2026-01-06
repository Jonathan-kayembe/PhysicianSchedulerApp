package com.saas.medicalapp.repository;

import com.saas.medicalapp.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Appointment Repository
 * JPA repository for Appointment entity
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findByUserId(Integer userId);
    
    @Query("SELECT a FROM Appointment a WHERE a.user.id = :userId AND a.appointmentTime >= :startTime AND a.appointmentTime < :endTime")
    List<Appointment> findByUserIdAndTimeRange(@Param("userId") Integer userId, 
                                                @Param("startTime") LocalDateTime startTime, 
                                                @Param("endTime") LocalDateTime endTime);
}

