package com.saas.medicalapp.repository;

import com.saas.medicalapp.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Assignment Repository
 * JPA repository for Assignment entity
 */
@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {
    
    @Query("SELECT a FROM Assignment a JOIN FETCH a.user u JOIN FETCH u.role JOIN FETCH a.appointment WHERE a.user.id = :userId")
    List<Assignment> findByUserId(@Param("userId") Integer userId);
    
    @Query("SELECT a FROM Assignment a JOIN FETCH a.user u JOIN FETCH u.role JOIN FETCH a.appointment WHERE a.appointment.id = :appointmentId")
    List<Assignment> findByAppointmentId(@Param("appointmentId") Integer appointmentId);
}

