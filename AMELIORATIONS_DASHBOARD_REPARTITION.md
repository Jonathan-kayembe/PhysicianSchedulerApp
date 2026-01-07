# Améliorations du Dashboard et Répartition des Patients

## 📋 Résumé des Améliorations

Ce document décrit les améliorations apportées pour :
1. Afficher les rendez-vous de la semaine (pas seulement aujourd'hui)
2. Répartir équitablement les patients entre le personnel médical

---

## ✅ 1. Affichage des Rendez-vous de la Semaine

### Modifications Frontend

#### `dashboard.html`
- ✅ Ajout d'une section "This Week's Appointments"
- ✅ Ajout de filtres par jour de la semaine (Lundi à Dimanche + "Toute la semaine")
- ✅ Interface utilisateur améliorée avec boutons de filtrage

#### `client.js`
- ✅ Fonction `displayWeekAppointments()` pour afficher les rendez-vous de la semaine
- ✅ Fonction `filterWeekAppointments(day, buttonElement)` pour filtrer par jour
- ✅ Tri automatique des rendez-vous par date/heure
- ✅ Affichage amélioré avec format de date français
- ✅ Gestion des erreurs améliorée

#### `style.css`
- ✅ Styles pour les boutons de filtrage (`.week-filter`, `.filter-btn`)
- ✅ Design responsive pour mobile
- ✅ États actifs/inactifs pour les boutons

### Fonctionnalités
- **Affichage de tous les rendez-vous de la semaine** : Les utilisateurs peuvent voir tous leurs rendez-vous de la semaine en cours
- **Filtrage par jour** : Possibilité de filtrer les rendez-vous par jour de la semaine
- **Tri chronologique** : Les rendez-vous sont automatiquement triés par date et heure
- **Format de date amélioré** : Affichage en français avec jour de la semaine, date complète et heure

---

## ✅ 2. Répartition Automatique des Patients

### Nouveau Service : `PatientDistributionService`

#### Fonctionnalités Principales

1. **`findLeastLoadedMedicalStaff(locationId)`**
   - Trouve le personnel médical avec la charge de travail la plus faible
   - Prend en compte le nombre de patients assignés
   - Prend en compte le nombre de rendez-vous à venir
   - Peut filtrer par localisation

2. **`redistributeUnassignedPatients(locationId)`**
   - Répartit équitablement les patients non assignés
   - Réassigne les patients dont le responsable est inactif
   - Retourne le nombre de patients réassignés

3. **`rebalanceWorkload(locationId)`**
   - Rééquilibre la charge de travail entre tous les membres du personnel
   - Réassigne les patients si la charge est déséquilibrée (déviation > 25%)
   - Évite les réassignations inutiles

4. **`getWorkloadStatistics()`**
   - Retourne les statistiques de charge pour chaque membre du personnel
   - Format : `Map<String, Integer>` (Nom du personnel → Charge de travail)

### Modifications du `PatientService`

- ✅ **Attribution automatique** : Si `primaryMedicalResponsibleId` est `null`, le patient est automatiquement assigné au personnel le moins chargé
- ✅ Intégration avec `PatientDistributionService`
- ✅ Validation maintenue pour les assignations manuelles

### Nouveau Contrôleur : `PatientDistributionController`

Endpoints disponibles :

1. **`POST /patient-distribution/redistribute?locationId={id}`**
   - Répartit les patients non assignés
   - Paramètre optionnel : `locationId` pour filtrer par localisation

2. **`POST /patient-distribution/rebalance?locationId={id}`**
   - Rééquilibre la charge de travail
   - Paramètre optionnel : `locationId` pour filtrer par localisation

3. **`GET /patient-distribution/workload-stats`**
   - Obtient les statistiques de charge de travail

### Améliorations du `AssignmentService`

- ✅ Vérification améliorée des doublons (par rendez-vous, pas seulement par utilisateur)
- ✅ Prévention de l'assignation du responsable principal comme support
- ✅ Support du type d'attribution (`assignmentType`)
- ✅ Méthodes pour obtenir et supprimer les assignations

### Améliorations du `AssignmentController`

- ✅ Support du paramètre `assignmentType` lors de la création
- ✅ Endpoint pour obtenir les assignations d'un rendez-vous
- ✅ Endpoint pour supprimer une assignation
- ✅ Messages en français

### Améliorations des Repositories

#### `UserRepository`
- ✅ Nouvelle méthode `findAllMedicalStaff()` : Trouve tous les membres du personnel médical (Physicians et Nurses) actifs

#### `AssignmentRepository`
- ✅ Requêtes avec `JOIN FETCH` pour charger toutes les relations
- ✅ Évite les problèmes N+1

---

## 🎯 Utilisation

### Attribution Automatique lors de la Création d'un Patient

```json
POST /patients
{
  "fullName": "John Doe",
  "age": 45,
  "locationId": 1,
  "primaryMedicalResponsibleId": null,  // null = attribution automatique
  "medicalNotes": "Patient régulier"
}
```

**Réponse :**
```json
{
  "success": true,
  "message": "Patient créé avec attribution automatique au personnel le moins chargé",
  "autoAssigned": true,
  "assignedTo": "Dr. Michael Jordan",
  "patient": { ... }
}
```

### Répartition Manuelle

```bash
# Répartir les patients non assignés
POST /patient-distribution/redistribute

# Rééquilibrer la charge de travail
POST /patient-distribution/rebalance

# Obtenir les statistiques
GET /patient-distribution/workload-stats
```

---

## 📊 Calcul de la Charge de Travail

La charge de travail est calculée comme suit :
```
Charge = Nombre de patients assignés + Nombre de rendez-vous à venir (non annulés)
```

Le système choisit automatiquement le personnel avec la charge la plus faible pour les nouvelles assignations.

---

## 🔄 Rééquilibrage Automatique

Le système peut rééquilibrer la charge si :
- La charge d'un membre dépasse la moyenne de plus de 25%
- Il existe un autre membre avec une charge significativement plus faible

---

## 📝 Fichiers Modifiés

### Frontend
- `src/main/resources/static/client/dashboard.html`
- `src/main/resources/static/client/client.js`
- `src/main/resources/static/client/style.css`

### Backend
- `src/main/java/com/saas/medicalapp/repository/UserRepository.java`
- `src/main/java/com/saas/medicalapp/service/PatientService.java`
- `src/main/java/com/saas/medicalapp/service/AssignmentService.java`
- `src/main/java/com/saas/medicalapp/repository/AssignmentRepository.java`
- `src/main/java/com/saas/medicalapp/controller/PatientController.java`
- `src/main/java/com/saas/medicalapp/controller/AssignmentController.java`

### Nouveaux Fichiers
- `src/main/java/com/saas/medicalapp/service/PatientDistributionService.java`
- `src/main/java/com/saas/medicalapp/controller/PatientDistributionController.java`

---

## 🚀 Prochaines Étapes

1. **Redémarrer l'application Spring Boot**
2. **Tester le dashboard** : Vérifier que les rendez-vous de la semaine s'affichent correctement
3. **Tester les filtres** : Cliquer sur les différents jours de la semaine
4. **Tester l'attribution automatique** : Créer un patient sans spécifier de responsable médical
5. **Tester la répartition** : Utiliser les endpoints `/patient-distribution/*` pour rééquilibrer la charge

---

## 💡 Notes Importantes

- L'attribution automatique fonctionne uniquement si `primaryMedicalResponsibleId` est `null` ou non fourni
- La répartition prend en compte la localisation si spécifiée
- Le système évite de réassigner inutilement les patients (seulement si cela améliore l'équilibre)
- Les statistiques de charge incluent les patients ET les rendez-vous à venir

