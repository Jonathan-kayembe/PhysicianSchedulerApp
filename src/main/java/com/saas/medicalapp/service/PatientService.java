package com.saas.medicalapp.service;

import com.saas.medicalapp.model.Location;
import com.saas.medicalapp.model.Patient;
import com.saas.medicalapp.model.User;
import com.saas.medicalapp.repository.LocationRepository;
import com.saas.medicalapp.repository.PatientRepository;
import com.saas.medicalapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Patient Service
 * Business logic for patient management with medical responsible validation
 */
@Service
@Transactional
public class PatientService {
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private LocationRepository locationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }
    
    public Patient getPatientById(Integer id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }
    
    /**
     * Create a new patient with primary medical responsible
     * @param fullName Patient's full name
     * @param age Patient's age
     * @param locationId Location ID
     * @param primaryMedicalResponsibleId User ID of Physician or Nurse (must be medical role)
     * @param medicalNotes Medical notes
     * @return Created patient
     */
    public Patient createPatient(String fullName, Integer age, Integer locationId, 
                                 Integer primaryMedicalResponsibleId, String medicalNotes) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        
        User medicalResponsible = userRepository.findById(primaryMedicalResponsibleId)
                .orElseThrow(() -> new RuntimeException("Medical responsible not found"));
        
        // Validate that the user is a medical role (Physician or Nurse)
        if (medicalResponsible.getRole() == null || 
            !Boolean.TRUE.equals(medicalResponsible.getRole().getIsMedicalRole())) {
            throw new RuntimeException("Primary medical responsible must be a Physician or Nurse");
        }
        
        Patient patient = new Patient();
        patient.setFullName(fullName);
        patient.setAge(age);
        patient.setLocation(location);
        patient.setPrimaryMedicalResponsible(medicalResponsible);
        patient.setMedicalNotes(medicalNotes);
        
        return patientRepository.save(patient);
    }
    
    /**
     * Update patient information
     * @param id Patient ID
     * @param fullName Patient's full name
     * @param age Patient's age
     * @param locationId Location ID
     * @param primaryMedicalResponsibleId User ID of Physician or Nurse (must be medical role)
     * @param medicalNotes Medical notes
     * @return Updated patient
     */
    public Patient updatePatient(Integer id, String fullName, Integer age, Integer locationId,
                                 Integer primaryMedicalResponsibleId, String medicalNotes) {
        Patient patient = getPatientById(id);
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        
        if (primaryMedicalResponsibleId != null) {
            User medicalResponsible = userRepository.findById(primaryMedicalResponsibleId)
                    .orElseThrow(() -> new RuntimeException("Medical responsible not found"));
            
            // Validate that the user is a medical role
            if (medicalResponsible.getRole() == null || 
                !Boolean.TRUE.equals(medicalResponsible.getRole().getIsMedicalRole())) {
                throw new RuntimeException("Primary medical responsible must be a Physician or Nurse");
            }
            patient.setPrimaryMedicalResponsible(medicalResponsible);
        }
        
        patient.setFullName(fullName);
        patient.setAge(age);
        patient.setLocation(location);
        patient.setMedicalNotes(medicalNotes);
        
        return patientRepository.save(patient);
    }
}

