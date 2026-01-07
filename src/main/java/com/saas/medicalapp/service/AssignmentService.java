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
    
    public Assignment createAssignment(Integer userId, Integer appointmentId, String assignmentType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        // Check if assignment already exists (check by appointment, not just user)
        List<Assignment> existing = assignmentRepository.findByAppointmentId(appointmentId);
        for (Assignment a : existing) {
            if (a.getUser().getId().equals(userId)) {
                throw new RuntimeException("Assignment already exists for this user and appointment");
            }
        }
        
        // Ne pas permettre d'assigner le responsable principal comme support
        if (appointment.getUser() != null && appointment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Cannot assign primary responsible as support. User is already the primary responsible for this appointment.");
        }
        
        Assignment assignment = new Assignment();
        assignment.setUser(user);
        assignment.setAppointment(appointment);
        assignment.setAssignmentType(assignmentType != null ? assignmentType : "Support");
        
        return assignmentRepository.save(assignment);
    }
    
    public Assignment createAssignment(Integer userId, Integer appointmentId) {
        return createAssignment(userId, appointmentId, "Support");
    }
    
    public void deleteAssignment(Integer assignmentId) {
        assignmentRepository.deleteById(assignmentId);
    }
    
    public List<Assignment> getAppointmentAssignments(Integer appointmentId) {
        return assignmentRepository.findByAppointmentId(appointmentId);
    }
}

