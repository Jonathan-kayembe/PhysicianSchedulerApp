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
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "primary_medical_responsible_id", nullable = false)
    private User primaryMedicalResponsible;
    
    @Column(name = "medical_notes", columnDefinition = "TEXT")
    private String medicalNotes;
    
    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;
    
    @Column(name = "gender", length = 20)
    private String gender;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "emergency_contact", length = 100)
    private String emergencyContact;
    
    @Column(name = "emergency_phone", length = 20)
    private String emergencyPhone;
    
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<Appointment> appointments;
    
    // Constructors
    public Patient() {
    }
    
    public Patient(String fullName, Integer age, Location location, User primaryMedicalResponsible, String medicalNotes) {
        this.fullName = fullName;
        this.age = age;
        this.location = location;
        this.primaryMedicalResponsible = primaryMedicalResponsible;
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
    
    public User getPrimaryMedicalResponsible() {
        return primaryMedicalResponsible;
    }
    
    public void setPrimaryMedicalResponsible(User primaryMedicalResponsible) {
        this.primaryMedicalResponsible = primaryMedicalResponsible;
    }
    
    public java.time.LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(java.time.LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getEmergencyContact() {
        return emergencyContact;
    }
    
    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }
    
    public String getEmergencyPhone() {
        return emergencyPhone;
    }
    
    public void setEmergencyPhone(String emergencyPhone) {
        this.emergencyPhone = emergencyPhone;
    }
    
    public List<Appointment> getAppointments() {
        return appointments;
    }
    
    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }
}

