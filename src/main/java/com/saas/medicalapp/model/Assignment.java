package com.saas.medicalapp.model;

import jakarta.persistence.*;

/**
 * Assignment Entity
 * Junction table - allows multiple users to be assigned to the same appointment
 * (e.g., doctor + nurse)
 */
@Entity
@Table(name = "assignments")
public class Assignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;
    
    @Column(name = "assignment_type", length = 50)
    private String assignmentType; // Support, Assistant, Coordinator, etc.
    
    // Constructors
    public Assignment() {
    }
    
    public Assignment(User user, Appointment appointment) {
        this.user = user;
        this.appointment = appointment;
    }
    
    public Assignment(User user, Appointment appointment, String assignmentType) {
        this.user = user;
        this.appointment = appointment;
        this.assignmentType = assignmentType;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Appointment getAppointment() {
        return appointment;
    }
    
    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }
    
    public String getAssignmentType() {
        return assignmentType;
    }
    
    public void setAssignmentType(String assignmentType) {
        this.assignmentType = assignmentType;
    }
}

