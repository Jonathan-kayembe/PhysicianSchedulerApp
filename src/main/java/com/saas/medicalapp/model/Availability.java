package com.saas.medicalapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Availability Entity
 * Represents user availability time slots
 */
@Entity
@Table(name = "availability")
public class Availability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    
    @Column(name = "status", nullable = false)
    private String status; // "Available", "Busy", "Unavailable"
    
    @Column(name = "is_protected_time", nullable = false)
    private Boolean isProtectedTime = false;
    
    // Constructors
    public Availability() {
    }
    
    public Availability(User user, LocalDateTime startTime, LocalDateTime endTime, String status, Boolean isProtectedTime) {
        this.user = user;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.isProtectedTime = isProtectedTime;
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
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Boolean getIsProtectedTime() {
        return isProtectedTime;
    }
    
    public void setIsProtectedTime(Boolean isProtectedTime) {
        this.isProtectedTime = isProtectedTime;
    }
}

