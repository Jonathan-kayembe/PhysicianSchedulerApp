package com.saas.medicalapp.service;

import com.saas.medicalapp.model.Patient;
import com.saas.medicalapp.model.User;
import com.saas.medicalapp.repository.AppointmentRepository;
import com.saas.medicalapp.repository.PatientRepository;
import com.saas.medicalapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Patient Distribution Service
 * Service pour la répartition équitable des patients entre le personnel médical
 */
@Service
@Transactional
public class PatientDistributionService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    /**
     * Trouve le personnel médical avec la charge de travail la plus faible
     * @param locationId ID de la localisation (optionnel, null pour toutes les localisations)
     * @return User avec la charge la plus faible
     */
    public User findLeastLoadedMedicalStaff(Integer locationId) {
        List<User> medicalStaff = userRepository.findAllMedicalStaff();
        
        if (medicalStaff.isEmpty()) {
            throw new RuntimeException("Aucun personnel médical disponible");
        }
        
        // Calculer la charge de travail pour chaque membre du personnel
        Map<User, Integer> workloadMap = new HashMap<>();
        
        for (User staff : medicalStaff) {
            int workload = calculateWorkload(staff.getId(), locationId);
            workloadMap.put(staff, workload);
        }
        
        // Trouver le personnel avec la charge la plus faible
        return workloadMap.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(medicalStaff.get(0));
    }
    
    /**
     * Calcule la charge de travail d'un membre du personnel médical
     * @param userId ID de l'utilisateur
     * @param locationId ID de la localisation (optionnel)
     * @return Nombre de patients + rendez-vous actifs
     */
    private int calculateWorkload(Integer userId, Integer locationId) {
        // Compter les patients assignés
        List<Patient> patients = patientRepository.findAllWithRelations();
        long patientCount = patients.stream()
                .filter(p -> p.getPrimaryMedicalResponsible() != null && 
                            p.getPrimaryMedicalResponsible().getId().equals(userId))
                .filter(p -> locationId == null || 
                            (p.getLocation() != null && p.getLocation().getId().equals(locationId)))
                .count();
        
        // Compter les rendez-vous à venir (non annulés)
        long appointmentCount = appointmentRepository.findByUserId(userId).stream()
                .filter(a -> !"Cancelled".equals(a.getStatus()))
                .filter(a -> a.getAppointmentTime().isAfter(java.time.LocalDateTime.now()))
                .filter(a -> locationId == null || 
                            (a.getLocation() != null && a.getLocation().getId().equals(locationId)))
                .count();
        
        return (int) (patientCount + appointmentCount);
    }
    
    /**
     * Répartit équitablement les patients non assignés entre le personnel médical
     * @param locationId ID de la localisation (optionnel)
     * @return Nombre de patients réassignés
     */
    public int redistributeUnassignedPatients(Integer locationId) {
        List<Patient> patients = patientRepository.findAllWithRelations();
        
        // Filtrer les patients sans responsable médical ou avec responsable inactif
        List<Patient> unassignedPatients = patients.stream()
                .filter(p -> {
                    if (p.getPrimaryMedicalResponsible() == null) return true;
                    User responsible = p.getPrimaryMedicalResponsible();
                    return responsible.getIsActive() == null || !responsible.getIsActive();
                })
                .filter(p -> locationId == null || 
                            (p.getLocation() != null && p.getLocation().getId().equals(locationId)))
                .collect(Collectors.toList());
        
        if (unassignedPatients.isEmpty()) {
            return 0;
        }
        
        int redistributed = 0;
        for (Patient patient : unassignedPatients) {
            try {
                User leastLoaded = findLeastLoadedMedicalStaff(
                    patient.getLocation() != null ? patient.getLocation().getId() : null
                );
                patient.setPrimaryMedicalResponsible(leastLoaded);
                patientRepository.save(patient);
                redistributed++;
            } catch (Exception e) {
                System.err.println("Erreur lors de la réassignation du patient " + patient.getId() + ": " + e.getMessage());
            }
        }
        
        return redistributed;
    }
    
    /**
     * Rééquilibre la charge de travail entre tous les membres du personnel médical
     * @param locationId ID de la localisation (optionnel)
     * @return Nombre de patients réassignés
     */
    public int rebalanceWorkload(Integer locationId) {
        List<User> medicalStaff = userRepository.findAllMedicalStaff();
        if (medicalStaff.size() < 2) {
            return 0; // Pas besoin de rééquilibrer s'il n'y a qu'un seul membre
        }
        
        List<Patient> patients = patientRepository.findAllWithRelations();
        
        // Filtrer les patients selon la localisation
        List<Patient> relevantPatients = patients.stream()
                .filter(p -> locationId == null || 
                            (p.getLocation() != null && p.getLocation().getId().equals(locationId)))
                .collect(Collectors.toList());
        
        // Calculer la charge actuelle
        Map<Integer, Integer> currentWorkload = new HashMap<>();
        for (User staff : medicalStaff) {
            currentWorkload.put(staff.getId(), calculateWorkload(staff.getId(), locationId));
        }
        
        // Calculer la charge cible (moyenne)
        int totalWorkload = currentWorkload.values().stream().mapToInt(Integer::intValue).sum();
        int targetWorkload = totalWorkload / medicalStaff.size();
        int maxDeviation = targetWorkload / 4; // Permettre une déviation de 25%
        
        int reassigned = 0;
        
        // Rééquilibrer si la différence est trop grande
        for (Patient patient : relevantPatients) {
            if (patient.getPrimaryMedicalResponsible() == null) continue;
            
            Integer currentStaffId = patient.getPrimaryMedicalResponsible().getId();
            Integer currentStaffWorkload = currentWorkload.get(currentStaffId);
            
            // Si ce membre a trop de charge, trouver quelqu'un avec moins de charge
            if (currentStaffWorkload > targetWorkload + maxDeviation) {
                User leastLoaded = findLeastLoadedMedicalStaff(
                    patient.getLocation() != null ? patient.getLocation().getId() : null
                );
                
                // Ne réassigner que si cela améliore l'équilibre
                Integer leastLoadedWorkload = currentWorkload.get(leastLoaded.getId());
                if (leastLoadedWorkload < currentStaffWorkload - 1) {
                    patient.setPrimaryMedicalResponsible(leastLoaded);
                    patientRepository.save(patient);
                    
                    // Mettre à jour les charges
                    currentWorkload.put(currentStaffId, currentStaffWorkload - 1);
                    currentWorkload.put(leastLoaded.getId(), leastLoadedWorkload + 1);
                    reassigned++;
                }
            }
        }
        
        return reassigned;
    }
    
    /**
     * Obtient les statistiques de charge de travail pour tous les membres du personnel médical
     * @return Map avec User ID comme clé et charge de travail comme valeur
     */
    public Map<String, Integer> getWorkloadStatistics() {
        List<User> medicalStaff = userRepository.findAllMedicalStaff();
        Map<String, Integer> stats = new LinkedHashMap<>();
        
        for (User staff : medicalStaff) {
            int workload = calculateWorkload(staff.getId(), null);
            stats.put(staff.getFullName() + " (" + staff.getRole().getName() + ")", workload);
        }
        
        return stats;
    }
}

