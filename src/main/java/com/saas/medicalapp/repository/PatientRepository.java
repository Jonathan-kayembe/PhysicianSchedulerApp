package com.saas.medicalapp.repository;

import com.saas.medicalapp.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Patient Repository
 * JPA repository for Patient entity
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {
    
    /**
     * Find all patients with location and primary medical responsible (with role) loaded
     */
    @Query("SELECT DISTINCT p FROM Patient p " +
           "JOIN FETCH p.location " +
           "JOIN FETCH p.primaryMedicalResponsible pmr " +
           "JOIN FETCH pmr.role " +
           "ORDER BY p.fullName ASC")
    List<Patient> findAllWithRelations();
    
    /**
     * Find patient by ID with location and primary medical responsible (with role) loaded
     */
    @Query("SELECT p FROM Patient p " +
           "JOIN FETCH p.location " +
           "JOIN FETCH p.primaryMedicalResponsible pmr " +
           "JOIN FETCH pmr.role " +
           "WHERE p.id = :id")
    Optional<Patient> findByIdWithRelations(Integer id);
}

