-- ============================================================
-- Script de migration pour corriger la base de données existante
-- Ajoute les colonnes manquantes : is_active et created_at
-- Exécutez ce script UNE SEULE FOIS
-- ============================================================

USE medicaldb;

-- ============================================================
-- 1. Ajouter la colonne is_active si elle n'existe pas
-- ============================================================
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'medicaldb' 
    AND TABLE_NAME = 'users' 
    AND COLUMN_NAME = 'is_active'
);

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE users ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE',
    'SELECT "Colonne is_active existe déjà" AS Message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 2. Ajouter la colonne created_at si elle n'existe pas
-- ============================================================
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'medicaldb' 
    AND TABLE_NAME = 'users' 
    AND COLUMN_NAME = 'created_at'
);

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE users ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP',
    'SELECT "Colonne created_at existe déjà" AS Message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 3. Mettre à jour les utilisateurs existants
-- ============================================================
-- Désactiver temporairement le mode safe update pour permettre les mises à jour
SET SQL_SAFE_UPDATES = 0;

-- Mettre is_active = TRUE pour tous les utilisateurs où c'est NULL
UPDATE users SET is_active = TRUE WHERE is_active IS NULL;

-- Mettre created_at = CURRENT_TIMESTAMP pour tous les utilisateurs où c'est NULL
UPDATE users SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;

-- Réactiver le mode safe update
SET SQL_SAFE_UPDATES = 1;

-- ============================================================
-- 4. Vérification finale
-- ============================================================
SELECT '✅ Migration terminée avec succès!' AS Resultat;
SELECT COUNT(*) AS 'Nombre d''utilisateurs' FROM users;
