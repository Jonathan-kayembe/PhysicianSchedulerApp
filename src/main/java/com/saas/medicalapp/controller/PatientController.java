package com.saas.medicalapp.controller;

import com.saas.medicalapp.model.Patient;
import com.saas.medicalapp.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Patient Controller
 * REST endpoints for patient management
 */
@RestController
@RequestMapping("/patients")
@CrossOrigin(origins = "*")
public class PatientController {
    
    @Autowired
    private PatientService patientService;
    
    @GetMapping
    public ResponseEntity<List<Patient>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(patientService.getPatientById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPatient(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String fullName = (String) request.get("fullName");
            Integer age = Integer.valueOf(request.get("age").toString());
            Integer locationId = Integer.valueOf(request.get("locationId").toString());
            String medicalNotes = (String) request.get("medicalNotes");
            
            Patient patient = patientService.createPatient(fullName, age, locationId, medicalNotes);
            
            response.put("success", true);
            response.put("message", "Patient created successfully");
            response.put("patient", patient);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePatient(@PathVariable Integer id, 
                                                              @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String fullName = (String) request.get("fullName");
            Integer age = Integer.valueOf(request.get("age").toString());
            Integer locationId = Integer.valueOf(request.get("locationId").toString());
            String medicalNotes = (String) request.get("medicalNotes");
            
            Patient patient = patientService.updatePatient(id, fullName, age, locationId, medicalNotes);
            
            response.put("success", true);
            response.put("patient", patient);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

