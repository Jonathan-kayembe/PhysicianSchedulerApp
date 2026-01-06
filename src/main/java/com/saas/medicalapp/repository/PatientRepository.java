package com.saas.medicalapp.repository;

import com.saas.medicalapp.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Patient Repository
 * JPA repository for Patient entity
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {
}

