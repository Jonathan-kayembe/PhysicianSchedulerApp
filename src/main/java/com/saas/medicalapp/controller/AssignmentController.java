package com.saas.medicalapp.controller;

import com.saas.medicalapp.model.Assignment;
import com.saas.medicalapp.service.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Assignment Controller
 * REST endpoints for assignment management
 */
@RestController
@RequestMapping("/assignments")
public class AssignmentController {
    
    @Autowired
    private AssignmentService assignmentService;
    
    @GetMapping
    public ResponseEntity<List<Assignment>> getUserAssignments(@RequestParam Integer userId) {
        return ResponseEntity.ok(assignmentService.getUserAssignments(userId));
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAssignment(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = Integer.valueOf(request.get("userId").toString());
            Integer appointmentId = Integer.valueOf(request.get("appointmentId").toString());
            String assignmentType = request.get("assignmentType") != null ? 
                    (String) request.get("assignmentType") : "Support";
            
            Assignment assignment = assignmentService.createAssignment(userId, appointmentId, assignmentType);
            
            response.put("success", true);
            response.put("message", "Assignment créé avec succès");
            response.put("assignment", assignment);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<Assignment>> getAppointmentAssignments(@PathVariable Integer appointmentId) {
        try {
            return ResponseEntity.ok(assignmentService.getAppointmentAssignments(appointmentId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAssignment(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            assignmentService.deleteAssignment(id);
            response.put("success", true);
            response.put("message", "Assignment supprimé avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

