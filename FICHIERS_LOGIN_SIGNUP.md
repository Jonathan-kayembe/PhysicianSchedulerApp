# Fichiers qui Gèrent le Login et le Sign Up

## 📁 Structure des Fichiers

### 🔵 BACKEND (Java/Spring Boot)

#### 1. **Controller - Points d'entrée API**
📄 `src/main/java/com/saas/medicalapp/controller/AuthController.java`
- **Rôle** : Gère les endpoints REST pour l'authentification
- **Endpoints** :
  - `POST /auth/register` → Inscription (signup)
  - `POST /auth/login` → Connexion (login)
  - `GET /auth/logout` → Déconnexion
- **Fonctions** : Reçoit les requêtes HTTP, appelle le service, retourne les réponses JSON

#### 2. **Service - Logique Métier**
📄 `src/main/java/com/saas/medicalapp/service/AuthService.java`
- **Rôle** : Contient toute la logique d'authentification
- **Méthodes principales** :
  - `register(String fullName, String email, String password, Integer roleId)` → Inscription
  - `login(String email, String password)` → Connexion
  - `logout()` → Déconnexion
- **Fonctions** : Validation, hachage des mots de passe (BCrypt), vérification des credentials

#### 3. **Repository - Accès Base de Données**
📄 `src/main/java/com/saas/medicalapp/repository/UserRepository.java`
- **Rôle** : Interface JPA pour accéder à la table `users`
- **Méthodes utilisées** :
  - `findByEmailWithRole(String email)` → Trouve un utilisateur par email avec son rôle
  - `existsByEmail(String email)` → Vérifie si un email existe déjà
  - `save(User user)` → Sauvegarde un nouvel utilisateur

#### 4. **Modèle - Entité User**
📄 `src/main/java/com/saas/medicalapp/model/User.java`
- **Rôle** : Représente l'entité utilisateur en Java
- **Champs** : id, fullName, email, password, role, isActive, createdAt
- **Fonction** : Mapping entre Java et la table MySQL `users`

#### 5. **Modèle - Entité Role**
📄 `src/main/java/com/saas/medicalapp/model/Role.java`
- **Rôle** : Représente les rôles (Physician, Nurse, Staff, Manager, SuperAdmin)
- **Fonction** : Définit les rôles disponibles pour l'inscription

#### 6. **Configuration - Sécurité**
📄 `src/main/java/com/saas/medicalapp/config/SecurityConfig.java`
- **Rôle** : Configuration Spring Security
- **Fonctions** :
  - Configure le `PasswordEncoder` (BCrypt)
  - Configure CORS (Cross-Origin Resource Sharing)
  - Autorise l'accès aux endpoints `/auth/**` sans authentification

#### 7. **Configuration - CORS**
📄 `src/main/java/com/saas/medicalapp/config/CorsConfig.java`
- **Rôle** : Configuration CORS (actuellement désactivée, gérée par SecurityConfig)
- **Note** : Ce fichier est commenté car CORS est géré dans SecurityConfig

---

### 🟢 FRONTEND (HTML/JavaScript)

#### 8. **Page de Connexion**
📄 `frontend/client/login.html`
- **Rôle** : Interface utilisateur pour la connexion
- **Contenu** : Formulaire avec champs email et password
- **Script** : Appelle la fonction `login()` de `client.js`

#### 9. **Page d'Inscription**
📄 `frontend/client/signup.html`
- **Rôle** : Interface utilisateur pour l'inscription
- **Contenu** : Formulaire avec champs fullName, email, password, roleId
- **Script** : Appelle la fonction `register()` de `client.js`

#### 10. **JavaScript Client - Logique Frontend**
📄 `frontend/client/client.js`
- **Rôle** : Gère les appels API et l'interaction utilisateur
- **Fonctions principales** :
  - `login(event)` → Gère la soumission du formulaire de connexion
  - `register(event)` → Gère la soumission du formulaire d'inscription
  - `apiCall(endpoint, method, data)` → Fonction utilitaire pour les appels API
- **Fonctions** : 
  - Envoie les requêtes HTTP au backend
  - Gère les réponses (succès/erreur)
  - Redirige l'utilisateur après connexion/inscription
  - Stocke les informations utilisateur dans localStorage

#### 11. **Styles CSS**
📄 `frontend/client/style.css`
- **Rôle** : Styles pour les pages login et signup
- **Fonction** : Apparence visuelle des formulaires

---

### ⚙️ CONFIGURATION

#### 12. **Configuration Application**
📄 `src/main/resources/application.properties`
- **Rôle** : Configuration Spring Boot
- **Paramètres importants** :
  - URL de la base de données MySQL
  - Credentials de connexion
  - Configuration JPA/Hibernate
  - Port du serveur (8080)

---

## 🔄 Flux d'Exécution

### **SIGN UP (Inscription)**

```
1. Utilisateur remplit signup.html
   ↓
2. client.js → register() envoie POST /auth/register
   ↓
3. AuthController.signup() reçoit la requête
   ↓
4. AuthService.register() :
   - Valide les données
   - Vérifie si l'email existe (UserRepository.existsByEmail)
   - Récupère le rôle (RoleRepository.findById)
   - Hash le mot de passe (BCrypt)
   - Crée et sauvegarde l'utilisateur (UserRepository.save)
   ↓
5. AuthController retourne la réponse JSON
   ↓
6. client.js redirige vers dashboard.html
```

### **LOGIN (Connexion)**

```
1. Utilisateur remplit login.html
   ↓
2. client.js → login() envoie POST /auth/login
   ↓
3. AuthController.login() reçoit la requête
   ↓
4. AuthService.login() :
   - Valide les données
   - Trouve l'utilisateur (UserRepository.findByEmailWithRole)
   - Vérifie si l'utilisateur est actif
   - Compare le mot de passe (BCrypt ou plain text)
   ↓
5. AuthController retourne la réponse JSON
   ↓
6. client.js stocke l'utilisateur dans localStorage et redirige
```

---

## 📊 Résumé par Catégorie

### **Backend (7 fichiers)**
1. `AuthController.java` - Endpoints REST
2. `AuthService.java` - Logique métier
3. `UserRepository.java` - Accès base de données
4. `User.java` - Modèle utilisateur
5. `Role.java` - Modèle rôle
6. `SecurityConfig.java` - Configuration sécurité/CORS
7. `application.properties` - Configuration application

### **Frontend (4 fichiers)**
1. `login.html` - Page de connexion
2. `signup.html` - Page d'inscription
3. `client.js` - Logique JavaScript
4. `style.css` - Styles CSS

---

## 🎯 Fichiers à Modifier pour Déboguer

### **Pour le Login :**
1. `AuthService.java` (ligne 117) - Méthode `login()`
2. `AuthController.java` (ligne 92) - Endpoint `/auth/login`
3. `client.js` (ligne 103) - Fonction `login()`

### **Pour le Sign Up :**
1. `AuthService.java` (ligne 45) - Méthode `register()`
2. `AuthController.java` (ligne 28) - Endpoint `/auth/register`
3. `client.js` (ligne 36) - Fonction `register()`

---

## 📝 Notes Importantes

- **Base de données** : Les mots de passe sont stockés dans la table `users` (colonne `password`)
- **Sécurité** : Les nouveaux utilisateurs ont leurs mots de passe hashés avec BCrypt
- **Migration** : Le code supporte les mots de passe en clair (pour migration) et BCrypt
- **CORS** : Configuré pour autoriser les requêtes depuis `localhost:63342` et `localhost:8080`

