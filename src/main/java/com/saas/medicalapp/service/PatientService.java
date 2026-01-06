package com.saas.medicalapp.service;

import com.saas.medicalapp.model.Location;
import com.saas.medicalapp.model.Patient;
import com.saas.medicalapp.repository.LocationRepository;
import com.saas.medicalapp.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Patient Service
 * Business logic for patient management
 */
@Service
public class PatientService {
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private LocationRepository locationRepository;
    
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }
    
    public Patient getPatientById(Integer id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }
    
    public Patient createPatient(String fullName, Integer age, Integer locationId, String medicalNotes) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        
        Patient patient = new Patient();
        patient.setFullName(fullName);
        patient.setAge(age);
        patient.setLocation(location);
        patient.setMedicalNotes(medicalNotes);
        
        return patientRepository.save(patient);
    }
    
    public Patient updatePatient(Integer id, String fullName, Integer age, Integer locationId, String medicalNotes) {
        Patient patient = getPatientById(id);
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        
        patient.setFullName(fullName);
        patient.setAge(age);
        patient.setLocation(location);
        patient.setMedicalNotes(medicalNotes);
        
        return patientRepository.save(patient);
    }
}

