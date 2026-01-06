package com.saas.medicalapp.model;

import jakarta.persistence.*;
import java.util.List;

/**
 * Patient Entity
 * Represents patients in the system
 */
@Entity
@Table(name = "patients")
public class Patient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "full_name", nullable = false)
    private String fullName;
    
    @Column(name = "age", nullable = false)
    private Integer age;
    
    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
    
    @Column(name = "medical_notes", columnDefinition = "TEXT")
    private String medicalNotes;
    
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<Appointment> appointments;
    
    // Constructors
    public Patient() {
    }
    
    public Patient(String fullName, Integer age, Location location, String medicalNotes) {
        this.fullName = fullName;
        this.age = age;
        this.location = location;
        this.medicalNotes = medicalNotes;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public String getMedicalNotes() {
        return medicalNotes;
    }
    
    public void setMedicalNotes(String medicalNotes) {
        this.medicalNotes = medicalNotes;
    }
    
    public List<Appointment> getAppointments() {
        return appointments;
    }
    
    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }
}

