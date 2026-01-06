package com.saas.medicalapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Appointment Entity
 * Represents medical appointments
 */
@Entity
@Table(name = "appointments")
public class Appointment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    
    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
    
    @Column(name = "purpose", columnDefinition = "TEXT", nullable = false)
    private String purpose;
    
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;
    
    @Column(name = "priority", nullable = false)
    private String priority; // "Low", "Medium", "High", "Urgent"
    
    @Column(name = "status", nullable = false)
    private String status; // "Planned", "InProgress", "Completed", "Cancelled"
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "appointment_time", nullable = false)
    private LocalDateTime appointmentTime;
    
    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL)
    private List<Assignment> assignments;
    
    // Constructors
    public Appointment() {
    }
    
    public Appointment(User user, Patient patient, Location location, String purpose, 
                      Integer durationMinutes, String priority, String status, 
                      String notes, LocalDateTime appointmentTime) {
        this.user = user;
        this.patient = patient;
        this.location = location;
        this.purpose = purpose;
        this.durationMinutes = durationMinutes;
        this.priority = priority;
        this.status = status;
        this.notes = notes;
        this.appointmentTime = appointmentTime;
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
    
    public Patient getPatient() {
        return patient;
    }
    
    public void setPatient(Patient patient) {
        this.patient = patient;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public String getPurpose() {
        return purpose;
    }
    
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
    
    public Integer getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }
    
    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }
    
    public List<Assignment> getAssignments() {
        return assignments;
    }
    
    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }
}

