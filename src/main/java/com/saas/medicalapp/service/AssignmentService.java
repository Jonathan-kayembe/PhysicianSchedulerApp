package com.saas.medicalapp.service;

import com.saas.medicalapp.model.Appointment;
import com.saas.medicalapp.model.Assignment;
import com.saas.medicalapp.model.User;
import com.saas.medicalapp.repository.AppointmentRepository;
import com.saas.medicalapp.repository.AssignmentRepository;
import com.saas.medicalapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Assignment Service
 * Business logic for assignment management
 */
@Service
public class AssignmentService {
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    public List<Assignment> getUserAssignments(Integer userId) {
        return assignmentRepository.findByUserId(userId);
    }
    
    public Assignment createAssignment(Integer userId, Integer appointmentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        // Check if assignment already exists
        List<Assignment> existing = assignmentRepository.findByUserId(userId);
        for (Assignment a : existing) {
            if (a.getAppointment().getId().equals(appointmentId)) {
                throw new RuntimeException("Assignment already exists");
            }
        }
        
        Assignment assignment = new Assignment();
        assignment.setUser(user);
        assignment.setAppointment(appointment);
        
        return assignmentRepository.save(assignment);
    }
}

