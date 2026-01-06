package com.saas.medicalapp.controller;

import com.saas.medicalapp.model.Availability;
import com.saas.medicalapp.service.AvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Availability Controller
 * REST endpoints for availability management
 */
@RestController
@RequestMapping("/availability")
@CrossOrigin(origins = "*")
public class AvailabilityController {
    
    @Autowired
    private AvailabilityService availabilityService;
    
    @GetMapping
    public ResponseEntity<List<Availability>> getUserAvailabilities(@RequestParam Integer userId) {
        return ResponseEntity.ok(availabilityService.getUserAvailabilities(userId));
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAvailability(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = Integer.valueOf(request.get("userId").toString());
            LocalDateTime startTime = LocalDateTime.parse(request.get("startTime").toString());
            LocalDateTime endTime = LocalDateTime.parse(request.get("endTime").toString());
            String status = (String) request.get("status");
            Boolean isProtectedTime = request.get("isProtectedTime") != null ? 
                    Boolean.valueOf(request.get("isProtectedTime").toString()) : false;
            
            Availability availability = availabilityService.createAvailability(
                    userId, startTime, endTime, status, isProtectedTime);
            
            response.put("success", true);
            response.put("message", "Availability created successfully");
            response.put("availability", availability);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

