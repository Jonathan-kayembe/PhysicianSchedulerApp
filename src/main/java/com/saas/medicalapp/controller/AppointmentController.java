package com.saas.medicalapp.controller;

import com.saas.medicalapp.model.Appointment;
import com.saas.medicalapp.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Appointment Controller
 * REST endpoints for appointment management
 */
@RestController
@RequestMapping("/appointments")
public class AppointmentController {
    
    @Autowired
    private AppointmentService appointmentService;
    
    @GetMapping
    public ResponseEntity<?> getUserAppointments(@RequestParam Integer userId) {
        try {
            if (userId == null) {
                return ResponseEntity.badRequest().body("User ID is required");
            }
            List<Appointment> appointments = appointmentService.getUserAppointments(userId);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error loading appointments: " + e.getMessage());
            errorResponse.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAppointment(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = Integer.valueOf(request.get("userId").toString());
            Integer patientId = Integer.valueOf(request.get("patientId").toString());
            Integer locationId = Integer.valueOf(request.get("locationId").toString());
            String purpose = (String) request.get("purpose");
            Integer durationMinutes = Integer.valueOf(request.get("durationMinutes").toString());
            String priority = (String) request.get("priority");
            String status = (String) request.get("status");
            String notes = (String) request.get("notes");
            LocalDateTime appointmentTime = LocalDateTime.parse(request.get("appointmentTime").toString());
            
            // Check for overbooking
            String warning = appointmentService.checkOverbooking(userId, appointmentTime, durationMinutes);
            
            Appointment appointment = appointmentService.createAppointment(
                    userId, patientId, locationId, purpose, durationMinutes, 
                    priority, status, notes, appointmentTime);
            
            response.put("success", true);
            response.put("message", "Appointment created successfully");
            response.put("appointment", appointment);
            if (warning != null) {
                response.put("warning", warning);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateAppointmentStatus(@PathVariable Integer id, 
                                                                         @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String status = (String) request.get("status");
            Appointment appointment = appointmentService.updateAppointmentStatus(id, status);
            
            response.put("success", true);
            response.put("message", "Status updated successfully");
            response.put("appointment", appointment);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

