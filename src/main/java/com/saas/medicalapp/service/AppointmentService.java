package com.saas.medicalapp.service;

import com.saas.medicalapp.model.*;
import com.saas.medicalapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Appointment Service
 * Business logic for appointment management, including overbooking check
 */
@Service
public class AppointmentService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private LocationRepository locationRepository;
    
    @Autowired
    private AvailabilityRepository availabilityRepository;
    
    public List<Appointment> getUserAppointments(Integer userId) {
        return appointmentRepository.findByUserId(userId);
    }
    
    /**
     * Check for overbooking when creating an appointment
     * Returns warning message if overbooking is detected
     */
    public String checkOverbooking(Integer userId, LocalDateTime appointmentDate, Integer newDurationMinutes) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        
        // Get user availabilities for the appointment date
        List<Availability> availabilities = availabilityRepository.findByUserIdAndDate(userId, appointmentDate);
        
        if (availabilities.isEmpty()) {
            return "WARNING: No availability found for this date!";
        }
        
        // Check each availability slot
        for (Availability availability : availabilities) {
            LocalDateTime availabilityStart = availability.getStartTime();
            LocalDateTime availabilityEnd = availability.getEndTime();
            
            // Check if appointment time falls within this availability
            if (appointmentDate.isAfter(availabilityStart) || appointmentDate.isEqual(availabilityStart)) {
                LocalDateTime appointmentEnd = appointmentDate.plusMinutes(newDurationMinutes);
                
                if (appointmentEnd.isBefore(availabilityEnd) || appointmentEnd.isEqual(availabilityEnd)) {
                    // Calculate available time in minutes
                    long availableMinutes = ChronoUnit.MINUTES.between(availabilityStart, availabilityEnd);
                    
                    // Find all appointments in this time range
                    List<Appointment> existingAppointments = appointmentRepository
                            .findByUserIdAndTimeRange(userId, availabilityStart, availabilityEnd);
                    
                    // Calculate total scheduled time
                    int totalScheduledMinutes = existingAppointments.stream()
                            .mapToInt(Appointment::getDurationMinutes)
                            .sum();
                    
                    // Check if adding new appointment exceeds availability
                    if (totalScheduledMinutes + newDurationMinutes > availableMinutes) {
                        return String.format(
                            "WARNING: Overbooking detected! Available time: %d minutes, " +
                            "Scheduled time: %d minutes, New appointment: %d minutes. " +
                            "Total would be: %d minutes",
                            availableMinutes, totalScheduledMinutes, newDurationMinutes,
                            totalScheduledMinutes + newDurationMinutes
                        );
                    }
                }
            }
        }
        
        return null; // No overbooking
    }
    
    public Appointment createAppointment(Integer userId, Integer patientId, Integer locationId,
                                        String purpose, Integer durationMinutes, String priority,
                                        String status, String notes, LocalDateTime appointmentTime) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        
        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setPatient(patient);
        appointment.setLocation(location);
        appointment.setPurpose(purpose);
        appointment.setDurationMinutes(durationMinutes);
        appointment.setPriority(priority);
        appointment.setStatus(status);
        appointment.setNotes(notes);
        appointment.setAppointmentTime(appointmentTime);
        
        return appointmentRepository.save(appointment);
    }
    
    public Appointment updateAppointmentStatus(Integer id, String status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
    }
}

