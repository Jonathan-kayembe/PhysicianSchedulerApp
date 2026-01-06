package com.saas.medicalapp.service;

import com.saas.medicalapp.model.Availability;
import com.saas.medicalapp.model.User;
import com.saas.medicalapp.repository.AvailabilityRepository;
import com.saas.medicalapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Availability Service
 * Business logic for availability management
 */
@Service
public class AvailabilityService {
    
    @Autowired
    private AvailabilityRepository availabilityRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public List<Availability> getUserAvailabilities(Integer userId) {
        return availabilityRepository.findByUserId(userId);
    }
    
    public Availability createAvailability(Integer userId, LocalDateTime startTime, 
                                          LocalDateTime endTime, String status, Boolean isProtectedTime) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Availability availability = new Availability();
        availability.setUser(user);
        availability.setStartTime(startTime);
        availability.setEndTime(endTime);
        availability.setStatus(status);
        availability.setIsProtectedTime(isProtectedTime != null ? isProtectedTime : false);
        
        return availabilityRepository.save(availability);
    }
}

