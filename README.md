# Medical Scheduling SaaS Application

Application SaaS complète de planification médicale avec gestion des rendez-vous et des disponibilités.

## 🏗️ Structure du Projet

```
Projet_UA3_Final/
├── database_complete_schema.sql    # Schéma de base de données complet avec données d'exemple
├── backend/                         # Backend Spring Boot
│   ├── pom.xml                      # Dépendances Maven
│   └── src/main/
│       ├── java/com/saas/medicalapp/
│       │   ├── MedicalAppApplication.java    # Classe principale Spring Boot
│       │   ├── config/
│       │   │   └── CorsConfig.java          # Configuration CORS
│       │   ├── model/                       # Entités JPA
│       │   ├── repository/                  # Repositories JPA
│       │   ├── service/                     # Logique métier
│       │   └── controller/                  # Contrôleurs REST
│       └── resources/
│           └── application.properties       # Configuration
├── frontend/
│   ├── client/                             # Dashboard Client (Médecins, Infirmières, Personnel)
│   │   ├── login.html
│   │   ├── dashboard.html
│   │   ├── appointments.html
│   │   ├── patients.html
│   │   ├── task-detail.html
│   │   ├── style.css
│   │   └── client.js
│   └── manager/                            # Panneau Manager (Managers, Admins)
│       ├── dashboard.html
│       ├── users.html
│       ├── locations.html
│       ├── create-appointment.html
│       ├── analytics.html
│       ├── style.css
│       └── manager.js
└── README.md
```

## 🛠️ Technologies Utilisées

### Backend
- **Java 17**
- **Spring Boot 3.1.5**
- **Spring Data JPA** (accès à la base de données)
- **MySQL Connector** (pilote de base de données)
- **Maven** (gestion des dépendances)

### Frontend
- **HTML5**
- **CSS3** (thème beige, design simplifié)
- **JavaScript Vanilla** (sans frameworks)
- **Fetch API** (appels API)

### Base de données
- **MySQL** ou **MariaDB**
- **SQL** (définition du schéma et requêtes)

## 📊 Schéma de Base de Données

Le projet utilise 7 tables principales :
1. **roles** - Rôles des utilisateurs
2. **users** - Utilisateurs du système
3. **locations** - Lieux médicaux
4. **availability** - Disponibilités des utilisateurs
5. **patients** - Patients
6. **appointments** - Rendez-vous
7. **assignments** - Assignations (table de jonction)

Voir `database_complete_schema.sql` pour le schéma complet.

## 🚀 Installation et Configuration

### 1. Base de données

1. Créer une base de données MySQL nommée `medicaldb`:
```sql
CREATE DATABASE medicaldb;
```

2. Exécuter le script SQL:
```bash
mysql -u root -p medicaldb < database_complete_schema.sql
```

### 2. Configuration Backend

Modifier `src/main/resources/application.properties` si nécessaire:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/medicaldb?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=votre_mot_de_passe
```

### 3. Compilation et Exécution

```bash
# Compiler le projet
mvn clean install

# Lancer l'application
mvn spring-boot:run
```

Le backend sera accessible sur `http://localhost:8080`

### 4. Frontend

Ouvrir les fichiers HTML directement dans un navigateur ou utiliser un serveur local.

**Note:** Pour éviter les problèmes CORS, ouvrir les fichiers HTML via un serveur HTTP local (par exemple avec Live Server dans VS Code, ou Python `python -m http.server`).

## 🔑 Comptes de Test

Voir `database_complete_schema.sql` pour la liste complète.

**Exemples:**
- Médecin: `lebron.james@medical.com` / `lebron2024`
- Infirmière: `kylian.mbappe@medical.com` / `mbappe10`
- Manager: `zlatan.ibrahimovic@medical.com` / `zlatan10`
- Admin: `admin@medical.com` / `admin123`

## 🔌 API Endpoints

### Base URL: `http://localhost:8080`

- **POST** `/auth/register` - Inscription
- **POST** `/auth/login` - Connexion
- **GET** `/auth/logout` - Déconnexion
- **GET** `/users` - Liste des utilisateurs
- **GET** `/locations` - Liste des lieux
- **GET** `/availability?userId=1` - Disponibilités d'un utilisateur
- **GET** `/patients` - Liste des patients
- **GET** `/appointments?userId=1` - Rendez-vous d'un utilisateur
- **POST** `/appointments` - Créer un rendez-vous
- **PUT** `/appointments/{id}/status` - Mettre à jour le statut
- **GET** `/assignments?userId=1` - Assignations d'un utilisateur

Voir la documentation complète dans le fichier de spécification du projet.

## 🎨 Interface Utilisateur

### Client Dashboard
- Connexion
- Tableau de bord avec statistiques
- Liste des rendez-vous avec filtres
- Liste des patients
- Détails et mise à jour des rendez-vous

### Manager Panel
- Tableau de bord avec statistiques globales
- Gestion des utilisateurs
- Gestion des lieux
- Création de rendez-vous avec vérification de surréservation
- Analytiques et statistiques

## ⚠️ Fonctionnalités Clés

### Vérification de Surréservation
Le système vérifie automatiquement si un nouveau rendez-vous dépasse les disponibilités d'un utilisateur et affiche un avertissement.

### Gestion des Rôles
- **Physician** - Médecins
- **Nurse** - Infirmières
- **Staff** - Personnel
- **Manager** - Gestionnaires
- **SuperAdmin** - Administrateurs

Les utilisateurs sont redirigés vers l'interface appropriée selon leur rôle.

## 📝 Notes Importantes

- Les mots de passe ne sont **pas** hashés dans cette version (à implémenter en production)
- CORS est configuré pour `http://localhost:63342` (PhpStorm/WebStorm) et `http://localhost:8080`
- Le thème beige est utilisé pour un design simple et professionnel
- Tous les endpoints retournent du JSON

## 🐛 Dépannage

### Problèmes CORS
Si vous rencontrez des erreurs CORS, vérifiez que:
1. Le backend est bien lancé sur le port 8080
2. Les origines autorisées dans `CorsConfig.java` correspondent à votre URL frontend
3. Vous utilisez un serveur HTTP local pour servir les fichiers HTML

### Problèmes de base de données
1. Vérifiez que MySQL est lancé
2. Vérifiez les credentials dans `application.properties`
3. Vérifiez que la base de données `medicaldb` existe

## 📚 Documentation

Pour plus de détails sur la structure du projet, les endpoints API, et la logique métier, consultez la documentation complète fournie dans les spécifications du projet.

---

**Développé avec Spring Boot 3.1.5 et Java 17**

