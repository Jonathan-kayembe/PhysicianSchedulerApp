package com.saas.medicalapp.service;

import com.saas.medicalapp.model.Location;
import com.saas.medicalapp.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

/**
 * Location Service
 * Business logic for location management
 */
@Service
public class LocationService {
    
    @Autowired
    private LocationRepository locationRepository;
    
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }
    
    public Location getLocationById(Integer id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));
    }
    
    public Location createLocation(String name, String type, LocalTime openingHour, 
                                   LocalTime closingHour, Integer capacityPerDay) {
        Location location = new Location();
        location.setName(name);
        location.setType(type);
        location.setOpeningHour(openingHour);
        location.setClosingHour(closingHour);
        location.setCapacityPerDay(capacityPerDay);
        
        return locationRepository.save(location);
    }
    
    public Location updateLocation(Integer id, String name, String type, LocalTime openingHour, 
                                   LocalTime closingHour, Integer capacityPerDay) {
        Location location = getLocationById(id);
        location.setName(name);
        location.setType(type);
        location.setOpeningHour(openingHour);
        location.setClosingHour(closingHour);
        location.setCapacityPerDay(capacityPerDay);
        
        return locationRepository.save(location);
    }
}

