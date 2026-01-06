package com.saas.medicalapp.controller;

import com.saas.medicalapp.model.Location;
import com.saas.medicalapp.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Location Controller
 * REST endpoints for location management
 */
@RestController
@RequestMapping("/locations")
@CrossOrigin(origins = "*")
public class LocationController {
    
    @Autowired
    private LocationService locationService;
    
    @GetMapping
    public ResponseEntity<List<Location>> getAllLocations() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(locationService.getLocationById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createLocation(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String name = (String) request.get("name");
            String type = (String) request.get("type");
            LocalTime openingHour = LocalTime.parse(request.get("openingHour").toString());
            LocalTime closingHour = LocalTime.parse(request.get("closingHour").toString());
            Integer capacityPerDay = Integer.valueOf(request.get("capacityPerDay").toString());
            
            Location location = locationService.createLocation(name, type, openingHour, closingHour, capacityPerDay);
            
            response.put("success", true);
            response.put("message", "Location created successfully");
            response.put("location", location);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateLocation(@PathVariable Integer id, 
                                                               @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String name = (String) request.get("name");
            String type = (String) request.get("type");
            LocalTime openingHour = LocalTime.parse(request.get("openingHour").toString());
            LocalTime closingHour = LocalTime.parse(request.get("closingHour").toString());
            Integer capacityPerDay = Integer.valueOf(request.get("capacityPerDay").toString());
            
            Location location = locationService.updateLocation(id, name, type, openingHour, closingHour, capacityPerDay);
            
            response.put("success", true);
            response.put("location", location);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

