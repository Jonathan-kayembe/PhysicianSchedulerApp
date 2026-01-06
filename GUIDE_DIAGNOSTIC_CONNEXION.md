# Guide de Diagnostic pour les Problèmes de Connexion

## Problème : "Invalid email or password" lors de la connexion

### Informations importantes

**Utilisateur de test :**
- Email : `michael.jordan@medical.com`
- Mot de passe : `jordan23` (en clair dans la base de données)

---

## Étapes de Diagnostic

### 1. Vérifier que la base de données est à jour

Si votre base de données a été créée avant les dernières modifications, elle pourrait ne pas avoir les colonnes `is_active` et `created_at`.

**Solution :** Exécutez le script `update_database_schema.sql` :
```bash
mysql -u root -p medicaldb < update_database_schema.sql
```

Ou connectez-vous à MySQL et exécutez le script manuellement.

### 2. Vérifier les logs de l'application

Avec les modifications récentes, des logs de débogage ont été ajoutés. Vérifiez la console de l'application Spring Boot pour voir :

```
Login attempt for: michael.jordan@medical.com
Stored password length: 8
Stored password starts with: jord
Password is plain text, match result: true/false
Expected: 'jordan23', Received: '...'
```

Ces logs vous indiqueront :
- Si l'utilisateur est trouvé
- Le format du mot de passe stocké (BCrypt ou plain text)
- Si la comparaison réussit ou échoue

### 3. Vérifier la base de données directement

Connectez-vous à MySQL et vérifiez :

```sql
USE medicaldb;

-- Vérifier que l'utilisateur existe
SELECT id, full_name, email, password, role_id, is_active 
FROM users 
WHERE email = 'michael.jordan@medical.com';

-- Le résultat devrait montrer :
-- password = 'jordan23'
-- is_active = 1 (ou TRUE)
```

### 4. Vérifier les colonnes de la table

```sql
DESCRIBE users;
```

Vous devriez voir :
- `is_active` BOOLEAN
- `created_at` TIMESTAMP

Si ces colonnes n'existent pas, exécutez `update_database_schema.sql`.

### 5. Vérifier la connexion à la base de données

Dans `application.properties`, vérifiez :
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/medicaldb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=
```

Assurez-vous que :
- MySQL est en cours d'exécution
- La base de données `medicaldb` existe
- Les credentials sont corrects

### 6. Tester avec un autre utilisateur

Essayez de vous connecter avec un autre utilisateur de test :
- Email : `lebron.james@medical.com`
- Mot de passe : `lebron2024`

---

## Causes Possibles et Solutions

### Cause 1 : Colonnes manquantes dans la base de données
**Symptôme :** Erreur SQL lors de la requête
**Solution :** Exécuter `update_database_schema.sql`

### Cause 2 : Mot de passe incorrect
**Symptôme :** Logs montrent "Password mismatch"
**Solution :** Vérifier que vous utilisez exactement `jordan23` (sans espaces)

### Cause 3 : Email avec casse différente
**Symptôme :** "User not found"
**Solution :** Le code convertit l'email en minuscules, donc `Michael.Jordan@medical.com` devrait fonctionner aussi

### Cause 4 : Utilisateur inactif
**Symptôme :** "User account is inactive"
**Solution :** Vérifier dans la base de données :
```sql
UPDATE users SET is_active = TRUE WHERE email = 'michael.jordan@medical.com';
```

### Cause 5 : Problème de connexion à la base de données
**Symptôme :** "Database error during login"
**Solution :** 
- Vérifier que MySQL est démarré
- Vérifier les credentials dans `application.properties`
- Vérifier les logs Spring Boot pour plus de détails

---

## Test Rapide

Pour tester rapidement, vous pouvez utiliser curl :

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"michael.jordan@medical.com","password":"jordan23"}'
```

Réponse attendue (succès) :
```json
{
  "success": true,
  "message": "Login successful",
  "user": {
    "id": 2,
    "fullName": "Dr. Michael Jordan",
    "email": "michael.jordan@medical.com",
    "role": {
      "id": 1,
      "name": "Physician"
    }
  }
}
```

Réponse en cas d'erreur :
```json
{
  "success": false,
  "message": "Invalid email or password"
}
```

---

## Prochaines Étapes

1. **Exécutez le script de mise à jour** si nécessaire
2. **Redémarrez l'application Spring Boot**
3. **Vérifiez les logs** lors de la tentative de connexion
4. **Testez avec curl** pour isoler le problème (frontend vs backend)
5. **Vérifiez la base de données** directement si le problème persiste

---

## Notes Importantes

- Les mots de passe dans la base de données de test sont en **clair** (pour faciliter les tests)
- Le code supporte à la fois les mots de passe en clair et BCrypt
- Les logs de débogage seront supprimés en production
- Assurez-vous d'exécuter la migration des mots de passe en production

