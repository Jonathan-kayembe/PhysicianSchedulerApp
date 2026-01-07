package com.saas.medicalapp.controller;

import com.saas.medicalapp.service.PatientDistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Patient Distribution Controller
 * Endpoints pour la gestion de la répartition des patients
 */
@RestController
@RequestMapping("/patient-distribution")
public class PatientDistributionController {
    
    @Autowired
    private PatientDistributionService distributionService;
    
    /**
     * Répartit équitablement les patients non assignés
     * @param locationId ID de la localisation (optionnel, query parameter)
     * @return Nombre de patients réassignés
     */
    @PostMapping("/redistribute")
    public ResponseEntity<Map<String, Object>> redistributeUnassignedPatients(
            @RequestParam(required = false) Integer locationId) {
        Map<String, Object> response = new HashMap<>();
        try {
            int redistributed = distributionService.redistributeUnassignedPatients(locationId);
            response.put("success", true);
            response.put("message", redistributed + " patients ont été réassignés");
            response.put("redistributed", redistributed);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la redistribution: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Rééquilibre la charge de travail entre tous les membres du personnel médical
     * @param locationId ID de la localisation (optionnel, query parameter)
     * @return Nombre de patients réassignés
     */
    @PostMapping("/rebalance")
    public ResponseEntity<Map<String, Object>> rebalanceWorkload(
            @RequestParam(required = false) Integer locationId) {
        Map<String, Object> response = new HashMap<>();
        try {
            int reassigned = distributionService.rebalanceWorkload(locationId);
            response.put("success", true);
            response.put("message", reassigned + " patients ont été réassignés pour équilibrer la charge");
            response.put("reassigned", reassigned);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors du rééquilibrage: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Obtient les statistiques de charge de travail
     * @return Map avec les statistiques de charge par membre du personnel
     */
    @GetMapping("/workload-stats")
    public ResponseEntity<Map<String, Object>> getWorkloadStatistics() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Integer> stats = distributionService.getWorkloadStatistics();
            response.put("success", true);
            response.put("statistics", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération des statistiques: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

