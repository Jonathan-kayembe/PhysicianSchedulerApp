package com.saas.medicalapp.repository;

import com.saas.medicalapp.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Assignment Repository
 * JPA repository for Assignment entity
 */
@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {
    List<Assignment> findByUserId(Integer userId);
    List<Assignment> findByAppointmentId(Integer appointmentId);
}

