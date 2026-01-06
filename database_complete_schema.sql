-- Database for medical application
-- Improved schema with medical coherence:
-- - Every patient has a primary medical responsible (Physician or Nurse)
-- - Only medical roles (Physician, Nurse) can be primary responsible
-- - Staff cannot be primary medical responsible
-- - All relationships are normalized and consistent

-- First, I delete the old database if it exists
DROP DATABASE IF EXISTS medicaldb;

-- I create the new database
CREATE DATABASE medicaldb;

-- I tell MySQL to use this database
USE medicaldb;

-- Temporarily disable foreign key checks to avoid constraint errors when dropping tables
SET FOREIGN_KEY_CHECKS = 0;

-- I delete tables if they already exist (just in case)
-- Order is important: delete child tables first (those with foreign keys), then parent tables
DROP TABLE IF EXISTS assignments;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS availability;
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS locations;
DROP TABLE IF EXISTS roles;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Table for roles (physician, nurse, etc.)
-- Added is_medical_role flag to distinguish medical from administrative roles
CREATE TABLE roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    is_medical_role BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'True if role can be primary medical responsible'
);

-- Table for users
-- Each user has an id, a name, an email, a password and a role
-- role_id references the roles table (it's a foreign key)
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
);

-- Table for locations (hospitals, clinics, etc.)
-- I put opening and closing hours
-- capacity_per_day is the number of patients per day
CREATE TABLE locations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    opening_hour TIME NOT NULL,
    closing_hour TIME NOT NULL,
    capacity_per_day INT NOT NULL CHECK (capacity_per_day > 0),
    address VARCHAR(255),
    phone VARCHAR(20)
);

-- Table for availability
-- Each user can have multiple availability slots
-- start_time and end_time are the start and end times
-- status can be "Available", "Busy" or "Unavailable"
CREATE TABLE availability (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('Available', 'Busy', 'Unavailable')),
    is_protected_time BOOLEAN DEFAULT FALSE,
    notes TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CHECK (end_time > start_time),
    INDEX idx_user_time (user_id, start_time, end_time)
);

-- Table for patients
-- Each patient has a primary medical responsible (Physician or Nurse)
CREATE TABLE patients (
    id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    age INT NOT NULL CHECK (age >= 0 AND age <= 150),
    location_id INT NOT NULL,
    primary_medical_responsible_id INT NOT NULL COMMENT 'Must be Physician or Nurse',
    medical_notes TEXT,
    date_of_birth DATE,
    gender VARCHAR(20),
    phone VARCHAR(20),
    emergency_contact VARCHAR(100),
    emergency_phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE RESTRICT,
    FOREIGN KEY (primary_medical_responsible_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_primary_responsible (primary_medical_responsible_id),
    INDEX idx_location (location_id)
);

-- Table for appointments
-- This is the main table for appointments
-- user_id must be Physician or Nurse (primary medical responsible)
-- priority can be "Low", "Medium", "High" or "Urgent"
-- status can be "Planned", "InProgress", "Completed" or "Cancelled"
CREATE TABLE appointments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT 'Primary medical responsible - must be Physician or Nurse',
    patient_id INT NOT NULL,
    location_id INT NOT NULL,
    purpose TEXT NOT NULL,
    duration_minutes INT NOT NULL CHECK (duration_minutes > 0),
    priority VARCHAR(20) NOT NULL CHECK (priority IN ('Low', 'Medium', 'High', 'Urgent')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('Planned', 'InProgress', 'Completed', 'Cancelled')),
    notes TEXT,
    appointment_time DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE RESTRICT,
    FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE RESTRICT,
    INDEX idx_user_time (user_id, appointment_time),
    INDEX idx_patient_time (patient_id, appointment_time),
    INDEX idx_location_time (location_id, appointment_time)
);

-- Table for assignments
-- This table allows assigning multiple people to the same appointment
-- For example, a doctor and a nurse can be assigned to the same appointment
-- Staff can be assigned here for support, but primary responsible is in appointments.user_id
CREATE TABLE assignments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    appointment_id INT NOT NULL,
    assignment_type VARCHAR(50) DEFAULT 'Support' COMMENT 'Support, Assistant, Coordinator, etc.',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    UNIQUE KEY unique_assignment (user_id, appointment_id),
    INDEX idx_appointment (appointment_id)
);

-- ============================================================================
-- TRIGGERS FOR MEDICAL COHERENCE
-- ============================================================================

-- Trigger to ensure primary_medical_responsible_id is a medical role (Physician or Nurse)
DELIMITER //
CREATE TRIGGER check_patient_medical_responsible
BEFORE INSERT ON patients
FOR EACH ROW
BEGIN
    DECLARE role_is_medical BOOLEAN;
    SELECT is_medical_role INTO role_is_medical
    FROM roles r
    JOIN users u ON u.role_id = r.id
    WHERE u.id = NEW.primary_medical_responsible_id;
    
    IF role_is_medical = FALSE OR role_is_medical IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Primary medical responsible must be a Physician or Nurse';
    END IF;
END//
DELIMITER ;

-- Trigger to ensure appointments.user_id is a medical role
DELIMITER //
CREATE TRIGGER check_appointment_medical_responsible
BEFORE INSERT ON appointments
FOR EACH ROW
BEGIN
    DECLARE role_is_medical BOOLEAN;
    SELECT is_medical_role INTO role_is_medical
    FROM roles r
    JOIN users u ON u.role_id = r.id
    WHERE u.id = NEW.user_id;
    
    IF role_is_medical = FALSE OR role_is_medical IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Appointment primary responsible must be a Physician or Nurse';
    END IF;
END//
DELIMITER ;

-- Trigger to ensure appointments.user_id is a medical role on UPDATE
DELIMITER //
CREATE TRIGGER check_appointment_medical_responsible_update
BEFORE UPDATE ON appointments
FOR EACH ROW
BEGIN
    DECLARE role_is_medical BOOLEAN;
    SELECT is_medical_role INTO role_is_medical
    FROM roles r
    JOIN users u ON u.role_id = r.id
    WHERE u.id = NEW.user_id;
    
    IF role_is_medical = FALSE OR role_is_medical IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Appointment primary responsible must be a Physician or Nurse';
    END IF;
END//
DELIMITER ;

-- Now I will insert test data
-- I created users, patients, appointments, etc.

-- First, I create the roles with medical role flag
INSERT INTO roles (name, is_medical_role) VALUES
('Physician', TRUE),      -- id = 1
('Nurse', TRUE),          -- id = 2
('Staff', FALSE),         -- id = 3
('Manager', FALSE),       -- id = 4
('SuperAdmin', FALSE);    -- id = 5

-- I create locations (hospitals, clinics)
INSERT INTO locations (name, type, opening_hour, closing_hour, capacity_per_day, address, phone) VALUES
('Central Hospital', 'hospital', '08:00:00', '18:00:00', 50, '123 Medical Center Blvd', '555-0100'),
('Saint John Clinic', 'clinic', '09:00:00', '17:00:00', 30, '456 Health Street', '555-0200'),
('The Gardens Retirement Home', 'retirement_home', '07:00:00', '20:00:00', 20, '789 Elder Care Lane', '555-0300');

-- I create users
-- I put famous athletes' names to make it more fun
-- Each user has a different password (it's more secure that way)

-- Physicians (role 1) - IDs 1-8
INSERT INTO users (full_name, email, password, role_id) VALUES
('Dr. LeBron James', 'lebron.james@medical.com', 'lebron2024', 1),
('Dr. Michael Jordan', 'michael.jordan@medical.com', 'jordan23', 1),
('Dr. Kobe Bryant', 'kobe.bryant@medical.com', 'kobe24', 1),
('Dr. Lionel Messi', 'lionel.messi@medical.com', 'messi10', 1),
('Dr. Cristiano Ronaldo', 'cristiano.ronaldo@medical.com', 'ronaldo7', 1),
('Dr. Stephen Curry', 'stephen.curry@medical.com', 'curry30', 1),
('Dr. Kevin Durant', 'kevin.durant@medical.com', 'durant35', 1),
('Dr. Neymar Jr', 'neymar.jr@medical.com', 'neymar11', 1);

-- Nurses (role 2) - IDs 9-18
INSERT INTO users (full_name, email, password, role_id) VALUES
('Nurse Kylian Mbappé', 'kylian.mbappe@medical.com', 'mbappe10', 2),
('Nurse Erling Haaland', 'erling.haaland@medical.com', 'haaland9', 2),
('Nurse Giannis Antetokounmpo', 'giannis.antetokounmpo@medical.com', 'giannis34', 2),
('Nurse Luka Doncic', 'luka.doncic@medical.com', 'doncic77', 2),
('Nurse Karim Benzema', 'karim.benzema@medical.com', 'benzema9', 2),
('Nurse Joel Embiid', 'joel.embiid@medical.com', 'embiid21', 2),
('Nurse Mohamed Salah', 'mohamed.salah@medical.com', 'salah11', 2),
('Nurse Kawhi Leonard', 'kawhi.leonard@medical.com', 'leonard2', 2),
('Nurse Robert Lewandowski', 'robert.lewandowski@medical.com', 'lewandowski9', 2),
('Nurse Jayson Tatum', 'jayson.tatum@medical.com', 'tatum0', 2);

-- Medical staff (role 3) - IDs 19-28
INSERT INTO users (full_name, email, password, role_id) VALUES
('Staff Member Paul Pogba', 'paul.pogba@medical.com', 'pogba6', 3),
('Staff Member Anthony Davis', 'anthony.davis@medical.com', 'davis3', 3),
('Staff Member Virgil van Dijk', 'virgil.vandijk@medical.com', 'vandijk4', 3),
('Staff Member Russell Westbrook', 'russell.westbrook@medical.com', 'westbrook0', 3),
('Staff Member Sadio Mané', 'sadio.mane@medical.com', 'mane10', 3),
('Staff Member Damian Lillard', 'damian.lillard@medical.com', 'lillard0', 3),
('Staff Member Kevin De Bruyne', 'kevin.debruyne@medical.com', 'debruyne17', 3),
('Staff Member Klay Thompson', 'klay.thompson@medical.com', 'thompson11', 3),
('Staff Member Harry Kane', 'harry.kane@medical.com', 'kane9', 3),
('Staff Member Devin Booker', 'devin.booker@medical.com', 'booker1', 3);

-- Managers (role 4) - IDs 29-33
INSERT INTO users (full_name, email, password, role_id) VALUES
('Manager Zlatan Ibrahimovic', 'zlatan.ibrahimovic@medical.com', 'zlatan10', 4),
('Manager Magic Johnson', 'magic.johnson@medical.com', 'magic32', 4),
('Manager Sergio Ramos', 'sergio.ramos@medical.com', 'ramos4', 4),
('Manager Larry Bird', 'larry.bird@medical.com', 'bird33', 4),
('Manager Manuel Neuer', 'manuel.neuer@medical.com', 'neuer1', 4);

-- Admin (role 5) - ID 34
INSERT INTO users (full_name, email, password, role_id) VALUES
('Admin System', 'admin@medical.com', 'admin123', 5);

-- I create test patients with primary medical responsible
-- Each patient is assigned to a Physician or Nurse as primary responsible
INSERT INTO patients (full_name, age, location_id, primary_medical_responsible_id, medical_notes, date_of_birth, gender) VALUES
-- Assigned to Dr. LeBron James (ID 1)
('Jean Dupont', 65, 1, 1, 'Patient with hypertension. Regular follow-up required.', DATE_SUB(CURDATE(), INTERVAL 65 YEAR), 'Male'),
('Robert Smith', 72, 1, 1, 'Type 2 diabetes. Monthly blood glucose control required.', DATE_SUB(CURDATE(), INTERVAL 72 YEAR), 'Male'),
('Barbara Miller', 67, 1, 1, 'Hypertension and high cholesterol. Daily medication.', DATE_SUB(CURDATE(), INTERVAL 67 YEAR), 'Female'),
('Joseph Taylor', 63, 1, 1, 'Heart problems. Semiannual cardiology follow-up.', DATE_SUB(CURDATE(), INTERVAL 63 YEAR), 'Male'),
('Margaret Jackson', 71, 1, 1, 'Chronic kidney failure. Dialysis planned.', DATE_SUB(CURDATE(), INTERVAL 71 YEAR), 'Female'),

-- Assigned to Dr. Michael Jordan (ID 2)
('Paul Rousseau', 45, 2, 2, 'Routine consultation. No particular problem.', DATE_SUB(CURDATE(), INTERVAL 45 YEAR), 'Male'),
('Linda Garcia', 58, 2, 2, 'Rheumatoid arthritis. Quarterly rheumatology follow-up.', DATE_SUB(CURDATE(), INTERVAL 58 YEAR), 'Female'),
('Richard Davis', 55, 2, 2, 'Chronic asthma. Inhaler verification.', DATE_SUB(CURDATE(), INTERVAL 55 YEAR), 'Male'),
('Nancy Anderson', 75, 2, 2, 'Osteoporosis. Calcium and vitamin D supplementation.', DATE_SUB(CURDATE(), INTERVAL 75 YEAR), 'Female'),
('Carol Brown', 64, 2, 2, 'Depression. Monthly psychiatric follow-up.', DATE_SUB(CURDATE(), INTERVAL 64 YEAR), 'Female'),

-- Assigned to Dr. Kobe Bryant (ID 3)
('Thomas Wilson', 69, 1, 3, 'Sleep apnea. CPAP use.', DATE_SUB(CURDATE(), INTERVAL 69 YEAR), 'Male'),
('Charles White', 66, 2, 3, 'Vision problems. Ophthalmology consultation.', DATE_SUB(CURDATE(), INTERVAL 66 YEAR), 'Male'),
('Edward Martin', 62, 1, 3, 'Thyroid problems. Quarterly hormonal control.', DATE_SUB(CURDATE(), INTERVAL 62 YEAR), 'Male'),
('George Garcia', 68, 1, 3, 'COPD. Pulmonary rehabilitation in progress.', DATE_SUB(CURDATE(), INTERVAL 68 YEAR), 'Male'),

-- Assigned to Nurse Kylian Mbappé (ID 9) - for retirement home patients
('Marie Leblanc', 78, 3, 9, 'Retirement home resident. Needs walking assistance.', DATE_SUB(CURDATE(), INTERVAL 78 YEAR), 'Female'),
('William Jones', 81, 3, 9, 'Resident with memory disorders. Cognitive evaluation necessary.', DATE_SUB(CURDATE(), INTERVAL 81 YEAR), 'Male'),
('Susan Moore', 70, 3, 9, 'Resident with reduced mobility. Weekly physiotherapy.', DATE_SUB(CURDATE(), INTERVAL 70 YEAR), 'Female'),
('Daniel Martinez', 77, 3, 9, 'Resident with mild dementia. Family support necessary.', DATE_SUB(CURDATE(), INTERVAL 77 YEAR), 'Male'),
('Dorothy Harris', 79, 3, 9, 'Resident with incontinence. Personalized care plan.', DATE_SUB(CURDATE(), INTERVAL 79 YEAR), 'Female'),

-- Assigned to Nurse Erling Haaland (ID 10)
('Betty Thompson', 73, 2, 10, 'Osteoarthritis. Chronic pain management.', DATE_SUB(CURDATE(), INTERVAL 73 YEAR), 'Female');

-- I create availability for users
-- Availability for today
INSERT INTO availability (user_id, start_time, end_time, status, is_protected_time) VALUES
-- Physicians
(1, CONCAT(CURDATE(), ' 09:00:00'), CONCAT(CURDATE(), ' 17:00:00'), 'Available', FALSE),
(2, CONCAT(CURDATE(), ' 08:00:00'), CONCAT(CURDATE(), ' 16:00:00'), 'Available', FALSE),
(3, CONCAT(CURDATE(), ' 10:00:00'), CONCAT(CURDATE(), ' 18:00:00'), 'Available', FALSE),
(4, CONCAT(CURDATE(), ' 07:00:00'), CONCAT(CURDATE(), ' 15:00:00'), 'Available', FALSE),
(5, CONCAT(CURDATE(), ' 09:30:00'), CONCAT(CURDATE(), ' 17:30:00'), 'Available', FALSE),
(6, CONCAT(CURDATE(), ' 08:30:00'), CONCAT(CURDATE(), ' 16:30:00'), 'Available', FALSE),

-- Nurses
(9, CONCAT(CURDATE(), ' 08:00:00'), CONCAT(CURDATE(), ' 16:00:00'), 'Available', FALSE),
(10, CONCAT(CURDATE(), ' 07:00:00'), CONCAT(CURDATE(), ' 15:00:00'), 'Available', FALSE),
(11, CONCAT(CURDATE(), ' 09:00:00'), CONCAT(CURDATE(), ' 17:00:00'), 'Available', FALSE),
(12, CONCAT(CURDATE(), ' 10:00:00'), CONCAT(CURDATE(), ' 18:00:00'), 'Available', FALSE),
(13, CONCAT(CURDATE(), ' 08:30:00'), CONCAT(CURDATE(), ' 16:30:00'), 'Available', FALSE),
(14, CONCAT(CURDATE(), ' 06:00:00'), CONCAT(CURDATE(), ' 14:00:00'), 'Available', FALSE),
(15, CONCAT(CURDATE(), ' 12:00:00'), CONCAT(CURDATE(), ' 20:00:00'), 'Available', FALSE);

-- Availability for tomorrow too
INSERT INTO availability (user_id, start_time, end_time, status, is_protected_time) VALUES
-- Physicians tomorrow
(1, DATE_ADD(CONCAT(CURDATE(), ' 09:00:00'), INTERVAL 1 DAY), DATE_ADD(CONCAT(CURDATE(), ' 17:00:00'), INTERVAL 1 DAY), 'Available', FALSE),
(2, DATE_ADD(CONCAT(CURDATE(), ' 08:00:00'), INTERVAL 1 DAY), DATE_ADD(CONCAT(CURDATE(), ' 16:00:00'), INTERVAL 1 DAY), 'Available', FALSE),
(3, DATE_ADD(CONCAT(CURDATE(), ' 10:00:00'), INTERVAL 1 DAY), DATE_ADD(CONCAT(CURDATE(), ' 18:00:00'), INTERVAL 1 DAY), 'Available', FALSE),
-- Nurses tomorrow
(9, DATE_ADD(CONCAT(CURDATE(), ' 08:00:00'), INTERVAL 1 DAY), DATE_ADD(CONCAT(CURDATE(), ' 16:00:00'), INTERVAL 1 DAY), 'Available', FALSE),
(10, DATE_ADD(CONCAT(CURDATE(), ' 07:00:00'), INTERVAL 1 DAY), DATE_ADD(CONCAT(CURDATE(), ' 15:00:00'), INTERVAL 1 DAY), 'Available', FALSE),
(11, DATE_ADD(CONCAT(CURDATE(), ' 09:00:00'), INTERVAL 1 DAY), DATE_ADD(CONCAT(CURDATE(), ' 17:00:00'), INTERVAL 1 DAY), 'Available', FALSE);

-- I create test appointments (ONLY PHYSICIAN OR NURSE AS PRIMARY RESPONSIBLE)
-- Physicians appointments
INSERT INTO appointments (user_id, patient_id, location_id, purpose, duration_minutes, priority, status, notes, appointment_time) VALUES
-- Dr. LeBron James (ID 1)
(1, 1, 1, 'Follow-up consultation for hypertension', 30, 'Medium', 'Planned', 'Check blood pressure and adjust treatment if necessary.', CONCAT(CURDATE(), ' 10:00:00')),
(1, 4, 1, 'Cardiology consultation - Post-operative follow-up', 45, 'High', 'Planned', 'Check after cardiac intervention. Verify healing.', CONCAT(CURDATE(), ' 14:00:00')),
(1, 5, 1, 'General consultation - Hypertension and cholesterol', 30, 'Medium', 'InProgress', 'Review laboratory results.', CONCAT(CURDATE(), ' 11:30:00')),

-- Dr. Michael Jordan (ID 2)
(2, 6, 2, 'Rheumatology consultation - Arthritis', 40, 'High', 'Planned', 'Evaluate arthritis progression. Treatment adjustment.', CONCAT(CURDATE(), ' 09:00:00')),
(2, 8, 2, 'Pulmonology consultation - Asthma', 30, 'Medium', 'Planned', 'Verify correct use of inhalers.', CONCAT(CURDATE(), ' 13:00:00')),
(2, 10, 2, 'Endocrinology consultation - Osteoporosis', 25, 'Low', 'Completed', 'Prescribe calcium supplements.', CONCAT(CURDATE(), ' 15:30:00')),

-- Dr. Kobe Bryant (ID 3)
(3, 11, 1, 'Cardiology consultation - Heart problems', 50, 'High', 'Planned', 'Electrocardiogram and cardiac ultrasound.', CONCAT(CURDATE(), ' 10:30:00')),
(3, 12, 1, 'Pulmonology consultation - Sleep apnea', 35, 'Medium', 'Planned', 'Verify CPAP use and adjustment.', CONCAT(CURDATE(), ' 14:30:00')),
(3, 13, 1, 'Nephrology consultation - Kidney failure', 60, 'Urgent', 'Planned', 'Discussion about starting dialysis.', CONCAT(CURDATE(), ' 16:00:00')),

-- Dr. Lionel Messi (ID 4)
(4, 9, 2, 'Psychiatry consultation - Depression', 45, 'High', 'Planned', 'Monthly follow-up. Evaluate treatment effectiveness.', CONCAT(CURDATE(), ' 08:30:00')),
(4, 11, 2, 'Ophthalmology consultation - Vision problems', 30, 'Medium', 'Planned', 'Eye exam and glasses prescription.', CONCAT(CURDATE(), ' 11:00:00')),

-- Dr. Cristiano Ronaldo (ID 5)
(5, 13, 1, 'Endocrinology consultation - Thyroid problems', 30, 'Medium', 'Planned', 'Hormonal control and medication adjustment.', CONCAT(CURDATE(), ' 10:00:00')),
(5, 14, 1, 'General consultation - COPD', 40, 'High', 'Planned', 'Pulmonary function evaluation.', CONCAT(CURDATE(), ' 15:00:00')),

-- Dr. Stephen Curry (ID 6)
(6, 20, 2, 'Rheumatology consultation - Osteoarthritis', 35, 'Medium', 'Planned', 'Chronic pain management plan.', CONCAT(CURDATE(), ' 09:30:00')),
(6, 6, 2, 'Routine consultation', 25, 'Low', 'Planned', 'Annual routine exam.', CONCAT(CURDATE(), ' 14:00:00'));

-- Nurses appointments (home visits, nursing care)
INSERT INTO appointments (user_id, patient_id, location_id, purpose, duration_minutes, priority, status, notes, appointment_time) VALUES
-- Nurse Kylian Mbappé (ID 9) - Retirement home visits
(9, 15, 3, 'Home visit - Retirement home - Walking assistance', 45, 'High', 'Planned', 'Check general condition and walking assistance.', CONCAT(CURDATE(), ' 14:00:00')),
(9, 16, 3, 'Home visit - Cognitive evaluation', 50, 'High', 'Planned', 'Memory and orientation test. Cognitive disorder evaluation.', CONCAT(CURDATE(), ' 10:00:00')),
(9, 17, 3, 'Home visit - Physiotherapy', 60, 'Medium', 'Planned', 'Physiotherapy session for reduced mobility.', CONCAT(CURDATE(), ' 11:00:00')),

-- Nurse Erling Haaland (ID 10) - Hospital nursing care
(10, 1, 1, 'Blood pressure check and control', 20, 'Medium', 'Completed', 'Normal blood pressure. Medication effective.', CONCAT(CURDATE(), ' 08:30:00')),
(10, 4, 1, 'Blood test - Blood glucose control', 15, 'Medium', 'Planned', 'Blood sample for diabetes control.', CONCAT(CURDATE(), ' 09:15:00')),
(10, 5, 1, 'Medication injection - Cholesterol', 10, 'Low', 'Planned', 'Monthly injection for cholesterol control.', CONCAT(CURDATE(), ' 12:00:00')),

-- Nurse Giannis Antetokounmpo (ID 11)
(11, 7, 2, 'Blood test - Rheumatology control', 20, 'Medium', 'Planned', 'Sample for inflammatory markers analysis.', CONCAT(CURDATE(), ' 10:00:00')),
(11, 8, 2, 'Pulmonary function test - Asthma', 30, 'High', 'Planned', 'Spirometry for asthma evaluation.', CONCAT(CURDATE(), ' 13:30:00')),

-- Nurse Luka Doncic (ID 12)
(12, 10, 2, 'Vitamin D injection - Osteoporosis', 10, 'Low', 'Planned', 'Quarterly vitamin D injection.', CONCAT(CURDATE(), ' 11:00:00')),
(12, 9, 2, 'Blood test - General control', 15, 'Low', 'Completed', 'Routine blood work.', CONCAT(CURDATE(), ' 09:00:00')),

-- Nurse Karim Benzema (ID 13)
(13, 11, 1, 'Blood pressure and ECG - Heart problems', 40, 'High', 'Planned', 'Complete cardiac control.', CONCAT(CURDATE(), ' 09:30:00')),
(13, 13, 1, 'Dialysis preparation - Patient information', 45, 'Urgent', 'Planned', 'Explanation of dialysis process.', CONCAT(CURDATE(), ' 15:30:00')),

-- Nurse Joel Embiid (ID 14)
(14, 12, 1, 'CPAP control - Sleep apnea', 25, 'Medium', 'Planned', 'Verify CPAP use and adjustment.', CONCAT(CURDATE(), ' 07:30:00')),
(14, 18, 1, 'Blood test - Thyroid control', 15, 'Medium', 'Planned', 'Sample for TSH, T3, T4.', CONCAT(CURDATE(), ' 10:30:00')),

-- Nurse Mohamed Salah (ID 15)
(15, 9, 2, 'Psychiatric follow-up - Depression', 30, 'High', 'Planned', 'Verify medication intake.', CONCAT(CURDATE(), ' 13:00:00')),
(15, 11, 2, 'Vision test - Ophthalmology', 20, 'Medium', 'Planned', 'Basic vision test.', CONCAT(CURDATE(), ' 11:30:00'));

-- Appointments for tomorrow
INSERT INTO appointments (user_id, patient_id, location_id, purpose, duration_minutes, priority, status, notes, appointment_time) VALUES
(1, 6, 2, 'Follow-up consultation - Tomorrow', 30, 'Medium', 'Planned', 'Routine consultation scheduled for tomorrow.', DATE_ADD(CONCAT(CURDATE(), ' 10:00:00'), INTERVAL 1 DAY)),
(2, 7, 2, 'Rheumatology consultation - Tomorrow', 40, 'High', 'Planned', 'Arthritis follow-up.', DATE_ADD(CONCAT(CURDATE(), ' 09:00:00'), INTERVAL 1 DAY)),
(9, 15, 3, 'Home visit - Tomorrow', 45, 'High', 'Planned', 'Follow-up visit at retirement home.', DATE_ADD(CONCAT(CURDATE(), ' 14:00:00'), INTERVAL 1 DAY));

-- I create assignments
-- This allows assigning multiple people to the same appointment
-- Staff can be assigned here for support, but primary responsible is in appointments.user_id
INSERT INTO assignments (user_id, appointment_id, assignment_type) VALUES
-- Appointment 1 (Dr. LeBron James - Hypertension consultation)
(9, 1, 'Nursing Support'),   -- Nurse Kylian Mbappé
(19, 1, 'Administrative'),  -- Staff Paul Pogba

-- Appointment 2 (Dr. Michael Jordan - Cardiology consultation)
(10, 2, 'Nursing Support'),  -- Nurse Erling Haaland
(20, 2, 'Logistics'),        -- Staff Anthony Davis

-- Appointment 3 (Dr. LeBron James - General consultation)
(11, 3, 'Nursing Support'),  -- Nurse Giannis Antetokounmpo

-- Appointment 4 (Dr. Michael Jordan - Rheumatology consultation)
(12, 4, 'Nursing Support'),  -- Nurse Luka Doncic
(21, 4, 'Administrative'),   -- Staff Virgil van Dijk

-- Appointment 5 (Dr. Michael Jordan - Pulmonology consultation)
(13, 5, 'Nursing Support'),   -- Nurse Karim Benzema

-- Appointment 6 (Dr. Kobe Bryant - Cardiology consultation)
(14, 6, 'Nursing Support'),  -- Nurse Joel Embiid
(22, 6, 'Administrative'),   -- Staff Russell Westbrook

-- Appointment 7 (Dr. Kobe Bryant - Pulmonology consultation)
(15, 7, 'Nursing Support'),  -- Nurse Mohamed Salah

-- Appointment 8 (Dr. Lionel Messi - Psychiatry consultation)
(9, 8, 'Nursing Support'),   -- Nurse Kylian Mbappé

-- Appointment 9 (Dr. Lionel Messi - Ophthalmology consultation)
(10, 9, 'Nursing Support'), -- Nurse Erling Haaland

-- Appointment 10 (Dr. Cristiano Ronaldo - Endocrinology consultation)
(11, 10, 'Nursing Support'), -- Nurse Giannis Antetokounmpo

-- Appointment 11 (Dr. Cristiano Ronaldo - COPD consultation)
(12, 11, 'Nursing Support'), -- Nurse Luka Doncic

-- Appointment 12 (Dr. Stephen Curry - Rheumatology consultation)
(13, 12, 'Nursing Support'), -- Nurse Karim Benzema

-- Appointment 13 (Dr. Stephen Curry - Routine consultation)
(14, 13, 'Nursing Support'), -- Nurse Joel Embiid

-- Nurse appointments with staff support
(19, 14, 'Administrative'),  -- Staff Paul Pogba for Nurse Mbappé home visit
(20, 14, 'Logistics'),       -- Staff Anthony Davis
(21, 15, 'Social Support'),  -- Staff Virgil van Dijk for cognitive evaluation
(19, 16, 'Administrative'),  -- Staff Paul Pogba for blood pressure check
(22, 17, 'Administrative'),  -- Staff Russell Westbrook for pulmonary test
(19, 18, 'Administrative'),  -- Staff Paul Pogba for dialysis preparation
(20, 18, 'Logistics');       -- Staff Anthony Davis

-- That's it! I created all tables and inserted test data
-- 
-- To verify it works, you can do:
-- SELECT * FROM users;
-- SELECT * FROM appointments;
-- SELECT * FROM patients;
--
-- Verification queries:
-- Check all patients have medical responsible:
-- SELECT p.id, p.full_name, u.full_name as responsible_name, r.name as role
-- FROM patients p
-- JOIN users u ON p.primary_medical_responsible_id = u.id
-- JOIN roles r ON u.role_id = r.id
-- WHERE r.is_medical_role = FALSE;  -- Should return 0 rows
--
-- Check all appointments have medical responsible:
-- SELECT a.id, a.purpose, u.full_name as primary_responsible, r.name as role
-- FROM appointments a
-- JOIN users u ON a.user_id = u.id
-- JOIN roles r ON u.role_id = r.id
-- WHERE r.is_medical_role = FALSE;  -- Should return 0 rows
