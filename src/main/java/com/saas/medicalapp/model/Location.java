package com.saas.medicalapp.model;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.List;

/**
 * Location Entity
 * Represents medical facilities (hospitals, clinics, retirement homes, etc.)
 */
@Entity
@Table(name = "locations")
public class Location {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "type", nullable = false)
    private String type; // "hospital", "clinic", "retirement_home", "ltc", "other"
    
    @Column(name = "opening_hour", nullable = false)
    private LocalTime openingHour;
    
    @Column(name = "closing_hour", nullable = false)
    private LocalTime closingHour;
    
    @Column(name = "capacity_per_day", nullable = false)
    private Integer capacityPerDay;
    
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private List<Patient> patients;
    
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private List<Appointment> appointments;
    
    // Constructors
    public Location() {
    }
    
    public Location(String name, String type, LocalTime openingHour, LocalTime closingHour, Integer capacityPerDay) {
        this.name = name;
        this.type = type;
        this.openingHour = openingHour;
        this.closingHour = closingHour;
        this.capacityPerDay = capacityPerDay;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public LocalTime getOpeningHour() {
        return openingHour;
    }
    
    public void setOpeningHour(LocalTime openingHour) {
        this.openingHour = openingHour;
    }
    
    public LocalTime getClosingHour() {
        return closingHour;
    }
    
    public void setClosingHour(LocalTime closingHour) {
        this.closingHour = closingHour;
    }
    
    public Integer getCapacityPerDay() {
        return capacityPerDay;
    }
    
    public void setCapacityPerDay(Integer capacityPerDay) {
        this.capacityPerDay = capacityPerDay;
    }
    
    public List<Patient> getPatients() {
        return patients;
    }
    
    public void setPatients(List<Patient> patients) {
        this.patients = patients;
    }
    
    public List<Appointment> getAppointments() {
        return appointments;
    }
    
    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }
}

