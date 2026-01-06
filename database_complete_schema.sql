-- Database for medical application
-- I created this database for my project
-- This is my first database so it's simple

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
-- Child tables (with foreign keys):
DROP TABLE IF EXISTS assignments;      -- References: users, appointments
DROP TABLE IF EXISTS appointments;     -- References: users, patients, locations
DROP TABLE IF EXISTS availability;     -- References: users
DROP TABLE IF EXISTS patients;         -- References: locations
-- Parent tables (referenced by others):
DROP TABLE IF EXISTS users;            -- References: roles
DROP TABLE IF EXISTS locations;        -- No dependencies
DROP TABLE IF EXISTS roles;            -- No dependencies

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Table for roles (physician, nurse, etc.)
-- I put id that increments automatically
-- name is the role name (ex: "Physician", "Nurse")
CREATE TABLE roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE
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
    FOREIGN KEY (role_id) REFERENCES roles(id)
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
    capacity_per_day INT NOT NULL
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
    status VARCHAR(20) NOT NULL,
    is_protected_time BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Table for patients
-- I put the name, age, location where they are and medical notes
CREATE TABLE patients (
    id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    location_id INT NOT NULL,
    medical_notes TEXT,
    FOREIGN KEY (location_id) REFERENCES locations(id)
);

-- Table for appointments
-- This is the main table for appointments
-- I put all important info: who, with whom, where, when, why
-- priority can be "Low", "Medium", "High" or "Urgent"
-- status can be "Planned", "InProgress", "Completed" or "Cancelled"
CREATE TABLE appointments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    patient_id INT NOT NULL,
    location_id INT NOT NULL,
    purpose TEXT NOT NULL,
    duration_minutes INT NOT NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    notes TEXT,
    appointment_time DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (location_id) REFERENCES locations(id)
);

-- Table for assignments
-- This table allows assigning multiple people to the same appointment
-- For example, a doctor and a nurse can be assigned to the same appointment
CREATE TABLE assignments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    appointment_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);

-- Now I will insert test data
-- I created users, patients, appointments, etc.

-- First, I create the roles
INSERT INTO roles (name) VALUES
('Physician'),
('Nurse'),
('Staff'),
('Manager'),
('SuperAdmin');

-- I create locations (hospitals, clinics)
INSERT INTO locations (name, type, opening_hour, closing_hour, capacity_per_day) VALUES
('Central Hospital', 'hospital', '08:00:00', '18:00:00', 50),
('Saint John Clinic', 'clinic', '09:00:00', '17:00:00', 30),
('The Gardens Retirement Home', 'retirement_home', '07:00:00', '20:00:00', 20);

-- I create users
-- I put famous athletes' names to make it more fun
-- Each user has a different password (it's more secure that way)

-- Physicians (role 1)
INSERT INTO users (full_name, email, password, role_id) VALUES
('Dr. LeBron James', 'lebron.james@medical.com', 'lebron2024', 1),
('Dr. Michael Jordan', 'michael.jordan@medical.com', 'jordan23', 1),
('Dr. Kobe Bryant', 'kobe.bryant@medical.com', 'kobe24', 1),
('Dr. Lionel Messi', 'lionel.messi@medical.com', 'messi10', 1),
('Dr. Cristiano Ronaldo', 'cristiano.ronaldo@medical.com', 'ronaldo7', 1),
('Dr. Stephen Curry', 'stephen.curry@medical.com', 'curry30', 1),
('Dr. Kevin Durant', 'kevin.durant@medical.com', 'durant35', 1),
('Dr. Neymar Jr', 'neymar.jr@medical.com', 'neymar11', 1);

-- Nurses (role 2)
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

-- Medical staff (role 3)
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

-- Managers (role 4)
INSERT INTO users (full_name, email, password, role_id) VALUES
('Manager Zlatan Ibrahimovic', 'zlatan.ibrahimovic@medical.com', 'zlatan10', 4),
('Manager Magic Johnson', 'magic.johnson@medical.com', 'magic32', 4),
('Manager Sergio Ramos', 'sergio.ramos@medical.com', 'ramos4', 4),
('Manager Larry Bird', 'larry.bird@medical.com', 'bird33', 4),
('Manager Manuel Neuer', 'manuel.neuer@medical.com', 'neuer1', 4);

-- Admin (role 5)
INSERT INTO users (full_name, email, password, role_id) VALUES
('Admin System', 'admin@medical.com', 'admin123', 5);

-- I create test patients
INSERT INTO patients (full_name, age, location_id, medical_notes) VALUES
('Jean Dupont', 65, 1, 'Patient with hypertension. Regular follow-up required.'),
('Marie Leblanc', 78, 3, 'Retirement home resident. Needs walking assistance.'),
('Paul Rousseau', 45, 2, 'Routine consultation. No particular problem.'),
('Robert Smith', 72, 1, 'Type 2 diabetes. Monthly blood glucose control required.'),
('Linda Garcia', 58, 2, 'Rheumatoid arthritis. Quarterly rheumatology follow-up.'),
('William Jones', 81, 3, 'Resident with memory disorders. Cognitive evaluation necessary.'),
('Barbara Miller', 67, 1, 'Hypertension and high cholesterol. Daily medication.'),
('Richard Davis', 55, 2, 'Chronic asthma. Inhaler verification.'),
('Susan Moore', 70, 3, 'Resident with reduced mobility. Weekly physiotherapy.'),
('Joseph Taylor', 63, 1, 'Heart problems. Semiannual cardiology follow-up.'),
('Nancy Anderson', 75, 2, 'Osteoporosis. Calcium and vitamin D supplementation.'),
('Thomas Wilson', 69, 1, 'Sleep apnea. CPAP use.'),
('Carol Brown', 64, 2, 'Depression. Monthly psychiatric follow-up.'),
('Daniel Martinez', 77, 3, 'Resident with mild dementia. Family support necessary.'),
('Margaret Jackson', 71, 1, 'Chronic kidney failure. Dialysis planned.'),
('Charles White', 66, 2, 'Vision problems. Ophthalmology consultation.'),
('Dorothy Harris', 79, 3, 'Resident with incontinence. Personalized care plan.'),
('Edward Martin', 62, 1, 'Thyroid problems. Quarterly hormonal control.'),
('Betty Thompson', 73, 2, 'Osteoarthritis. Chronic pain management.'),
('George Garcia', 68, 1, 'COPD. Pulmonary rehabilitation in progress.');

-- I create availability for users
-- Availability for today
INSERT INTO availability (user_id, start_time, end_time, status, is_protected_time) VALUES
-- Dr. LeBron James (ID 1)
(1, CONCAT(CURDATE(), ' 09:00:00'), CONCAT(CURDATE(), ' 17:00:00'), 'Available', FALSE),
-- Dr. Michael Jordan (ID 2)
(2, CONCAT(CURDATE(), ' 08:00:00'), CONCAT(CURDATE(), ' 16:00:00'), 'Available', FALSE),
-- Dr. Kobe Bryant (ID 3)
(3, CONCAT(CURDATE(), ' 10:00:00'), CONCAT(CURDATE(), ' 18:00:00'), 'Available', FALSE),
-- Dr. Lionel Messi (ID 4)
(4, CONCAT(CURDATE(), ' 07:00:00'), CONCAT(CURDATE(), ' 15:00:00'), 'Available', FALSE),
-- Dr. Cristiano Ronaldo (ID 5)
(5, CONCAT(CURDATE(), ' 09:30:00'), CONCAT(CURDATE(), ' 17:30:00'), 'Available', FALSE),
-- Dr. Stephen Curry (ID 6)
(6, CONCAT(CURDATE(), ' 08:30:00'), CONCAT(CURDATE(), ' 16:30:00'), 'Available', FALSE);

-- Nurses availability
INSERT INTO availability (user_id, start_time, end_time, status, is_protected_time) VALUES
-- Nurse Kylian Mbappé (ID 9)
(9, CONCAT(CURDATE(), ' 08:00:00'), CONCAT(CURDATE(), ' 16:00:00'), 'Available', FALSE),
-- Nurse Erling Haaland (ID 10)
(10, CONCAT(CURDATE(), ' 07:00:00'), CONCAT(CURDATE(), ' 15:00:00'), 'Available', FALSE),
-- Nurse Giannis Antetokounmpo (ID 11)
(11, CONCAT(CURDATE(), ' 09:00:00'), CONCAT(CURDATE(), ' 17:00:00'), 'Available', FALSE),
-- Nurse Luka Doncic (ID 12)
(12, CONCAT(CURDATE(), ' 10:00:00'), CONCAT(CURDATE(), ' 18:00:00'), 'Available', FALSE),
-- Nurse Karim Benzema (ID 13)
(13, CONCAT(CURDATE(), ' 08:30:00'), CONCAT(CURDATE(), ' 16:30:00'), 'Available', FALSE),
-- Nurse Joel Embiid (ID 14)
(14, CONCAT(CURDATE(), ' 06:00:00'), CONCAT(CURDATE(), ' 14:00:00'), 'Available', FALSE),
-- Nurse Mohamed Salah (ID 15)
(15, CONCAT(CURDATE(), ' 12:00:00'), CONCAT(CURDATE(), ' 20:00:00'), 'Available', FALSE);

-- Staff availability
INSERT INTO availability (user_id, start_time, end_time, status, is_protected_time) VALUES
-- Staff Member Paul Pogba (ID 19)
(19, CONCAT(CURDATE(), ' 10:00:00'), CONCAT(CURDATE(), ' 18:00:00'), 'Available', FALSE),
-- Staff Member Anthony Davis (ID 20)
(20, CONCAT(CURDATE(), ' 08:00:00'), CONCAT(CURDATE(), ' 16:00:00'), 'Available', FALSE),
-- Staff Member Virgil van Dijk (ID 21)
(21, CONCAT(CURDATE(), ' 09:00:00'), CONCAT(CURDATE(), ' 17:00:00'), 'Available', FALSE),
-- Staff Member Russell Westbrook (ID 22)
(22, CONCAT(CURDATE(), ' 07:00:00'), CONCAT(CURDATE(), ' 15:00:00'), 'Available', FALSE);

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

-- I create test appointments
-- Appointments for today
INSERT INTO appointments (user_id, patient_id, location_id, purpose, duration_minutes, priority, status, notes, appointment_time) VALUES
-- Dr. LeBron James (ID 1)
(1, 1, 1, 'Follow-up consultation for hypertension', 30, 'Medium', 'Planned', 'Check blood pressure and adjust treatment if necessary.', CONCAT(CURDATE(), ' 10:00:00')),
(1, 4, 1, 'Cardiology consultation - Post-operative follow-up', 45, 'High', 'Planned', 'Check after cardiac intervention. Verify healing.', CONCAT(CURDATE(), ' 14:00:00')),
(1, 7, 1, 'General consultation - Hypertension and cholesterol', 30, 'Medium', 'InProgress', 'Review laboratory results.', CONCAT(CURDATE(), ' 11:30:00')),

-- Dr. Michael Jordan (ID 2)
(2, 5, 2, 'Rheumatology consultation - Arthritis', 40, 'High', 'Planned', 'Evaluate arthritis progression. Treatment adjustment.', CONCAT(CURDATE(), ' 09:00:00')),
(2, 8, 2, 'Pulmonology consultation - Asthma', 30, 'Medium', 'Planned', 'Verify correct use of inhalers.', CONCAT(CURDATE(), ' 13:00:00')),
(2, 11, 2, 'Endocrinology consultation - Osteoporosis', 25, 'Low', 'Completed', 'Prescribe calcium supplements.', CONCAT(CURDATE(), ' 15:30:00')),

-- Dr. Kobe Bryant (ID 3)
(3, 10, 1, 'Cardiology consultation - Heart problems', 50, 'High', 'Planned', 'Electrocardiogram and cardiac ultrasound.', CONCAT(CURDATE(), ' 10:30:00')),
(3, 12, 1, 'Pulmonology consultation - Sleep apnea', 35, 'Medium', 'Planned', 'Verify CPAP use and adjustment.', CONCAT(CURDATE(), ' 14:30:00')),
(3, 15, 1, 'Nephrology consultation - Kidney failure', 60, 'Urgent', 'Planned', 'Discussion about starting dialysis.', CONCAT(CURDATE(), ' 16:00:00')),

-- Dr. Lionel Messi (ID 4)
(4, 13, 2, 'Psychiatry consultation - Depression', 45, 'High', 'Planned', 'Monthly follow-up. Evaluate treatment effectiveness.', CONCAT(CURDATE(), ' 08:30:00')),
(4, 16, 2, 'Ophthalmology consultation - Vision problems', 30, 'Medium', 'Planned', 'Eye exam and glasses prescription.', CONCAT(CURDATE(), ' 11:00:00')),

-- Dr. Cristiano Ronaldo (ID 5)
(5, 18, 1, 'Endocrinology consultation - Thyroid problems', 30, 'Medium', 'Planned', 'Hormonal control and medication adjustment.', CONCAT(CURDATE(), ' 10:00:00')),
(5, 20, 1, 'General consultation - COPD', 40, 'High', 'Planned', 'Pulmonary function evaluation.', CONCAT(CURDATE(), ' 15:00:00')),

-- Dr. Stephen Curry (ID 6)
(6, 19, 2, 'Rheumatology consultation - Osteoarthritis', 35, 'Medium', 'Planned', 'Chronic pain management plan.', CONCAT(CURDATE(), ' 09:30:00')),
(6, 3, 2, 'Routine consultation', 25, 'Low', 'Planned', 'Annual routine exam.', CONCAT(CURDATE(), ' 14:00:00'));

-- Nurses appointments
INSERT INTO appointments (user_id, patient_id, location_id, purpose, duration_minutes, priority, status, notes, appointment_time) VALUES
-- Nurse Kylian Mbappé (ID 9)
(9, 2, 3, 'Home visit - Retirement home - Walking assistance', 45, 'High', 'Planned', 'Check general condition and walking assistance.', CONCAT(CURDATE(), ' 14:00:00')),
(9, 6, 3, 'Home visit - Cognitive evaluation', 50, 'High', 'Planned', 'Memory and orientation test. Cognitive disorder evaluation.', CONCAT(CURDATE(), ' 10:00:00')),
(9, 9, 3, 'Home visit - Physiotherapy', 60, 'Medium', 'Planned', 'Physiotherapy session for reduced mobility.', CONCAT(CURDATE(), ' 11:00:00')),

-- Nurse Erling Haaland (ID 10)
(10, 1, 1, 'Blood pressure check and control', 20, 'Medium', 'Completed', 'Normal blood pressure. Medication effective.', CONCAT(CURDATE(), ' 08:30:00')),
(10, 4, 1, 'Blood test - Blood glucose control', 15, 'Medium', 'Planned', 'Blood sample for diabetes control.', CONCAT(CURDATE(), ' 09:15:00')),
(10, 7, 1, 'Medication injection - Cholesterol', 10, 'Low', 'Planned', 'Monthly injection for cholesterol control.', CONCAT(CURDATE(), ' 12:00:00')),

-- Nurse Giannis Antetokounmpo (ID 11)
(11, 5, 2, 'Blood test - Rheumatology control', 20, 'Medium', 'Planned', 'Sample for inflammatory markers analysis.', CONCAT(CURDATE(), ' 10:00:00')),
(11, 8, 2, 'Pulmonary function test - Asthma', 30, 'High', 'Planned', 'Spirometry for asthma evaluation.', CONCAT(CURDATE(), ' 13:30:00')),

-- Nurse Luka Doncic (ID 12)
(12, 11, 2, 'Vitamin D injection - Osteoporosis', 10, 'Low', 'Planned', 'Quarterly vitamin D injection.', CONCAT(CURDATE(), ' 11:00:00')),
(12, 14, 2, 'Blood test - General control', 15, 'Low', 'Completed', 'Routine blood work.', CONCAT(CURDATE(), ' 09:00:00')),

-- Nurse Karim Benzema (ID 13)
(13, 10, 1, 'Blood pressure and ECG - Heart problems', 40, 'High', 'Planned', 'Complete cardiac control.', CONCAT(CURDATE(), ' 09:30:00')),
(13, 15, 1, 'Dialysis preparation - Patient information', 45, 'Urgent', 'Planned', 'Explanation of dialysis process.', CONCAT(CURDATE(), ' 15:30:00')),

-- Nurse Joel Embiid (ID 14)
(14, 12, 1, 'CPAP control - Sleep apnea', 25, 'Medium', 'Planned', 'Verify CPAP use and adjustment.', CONCAT(CURDATE(), ' 07:30:00')),
(14, 18, 1, 'Blood test - Thyroid control', 15, 'Medium', 'Planned', 'Sample for TSH, T3, T4.', CONCAT(CURDATE(), ' 10:30:00')),

-- Nurse Mohamed Salah (ID 15)
(15, 13, 2, 'Psychiatric follow-up - Depression', 30, 'High', 'Planned', 'Verify medication intake.', CONCAT(CURDATE(), ' 13:00:00')),
(15, 16, 2, 'Vision test - Ophthalmology', 20, 'Medium', 'Planned', 'Basic vision test.', CONCAT(CURDATE(), ' 11:30:00'));

-- Staff appointments
INSERT INTO appointments (user_id, patient_id, location_id, purpose, duration_minutes, priority, status, notes, appointment_time) VALUES
-- Staff Member Paul Pogba (ID 19)
(19, 17, 3, 'Administrative support - Care plan', 30, 'Low', 'Planned', 'Update personalized care plan.', CONCAT(CURDATE(), ' 11:00:00')),
(19, 6, 3, 'Administrative support - Medical file', 20, 'Low', 'Completed', 'Update medical file.', CONCAT(CURDATE(), ' 14:30:00')),

-- Staff Member Anthony Davis (ID 20)
(20, 1, 1, 'Logistics support - Medical transport', 15, 'Low', 'Planned', 'Organize transport for next consultation.', CONCAT(CURDATE(), ' 16:00:00')),
(20, 4, 1, 'Administrative support - Appointments', 10, 'Low', 'Completed', 'Schedule appointment for specialized consultation.', CONCAT(CURDATE(), ' 08:00:00')),

-- Staff Member Virgil van Dijk (ID 21)
(21, 2, 3, 'Social support - Home care', 40, 'Medium', 'Planned', 'Coordinate home care services.', CONCAT(CURDATE(), ' 10:00:00')),
(21, 9, 3, 'Administrative support - Insurance', 25, 'Low', 'Planned', 'Verify insurance coverage.', CONCAT(CURDATE(), ' 15:00:00'));

-- Some appointments for tomorrow too
INSERT INTO appointments (user_id, patient_id, location_id, purpose, duration_minutes, priority, status, notes, appointment_time) VALUES
(1, 3, 2, 'Follow-up consultation - Tomorrow', 30, 'Medium', 'Planned', 'Routine consultation scheduled for tomorrow.', DATE_ADD(CONCAT(CURDATE(), ' 10:00:00'), INTERVAL 1 DAY)),
(2, 5, 2, 'Rheumatology consultation - Tomorrow', 40, 'High', 'Planned', 'Arthritis follow-up.', DATE_ADD(CONCAT(CURDATE(), ' 09:00:00'), INTERVAL 1 DAY)),
(9, 2, 3, 'Home visit - Tomorrow', 45, 'High', 'Planned', 'Follow-up visit at retirement home.', DATE_ADD(CONCAT(CURDATE(), ' 14:00:00'), INTERVAL 1 DAY));

-- I create assignments
-- This allows assigning multiple people to the same appointment
INSERT INTO assignments (user_id, appointment_id) VALUES
-- Appointment 1 (Dr. LeBron James - Hypertension consultation)
(9, 1),   -- Nurse Kylian Mbappé assigned
(19, 1),  -- Staff Paul Pogba assigned
-- Appointment 2 (Dr. Michael Jordan - Cardiology consultation)
(10, 2),  -- Nurse Erling Haaland assigned
(20, 2),  -- Staff Anthony Davis assigned
-- Appointment 3 (Dr. LeBron James - General consultation)
(11, 3),  -- Nurse Giannis Antetokounmpo assigned
-- Appointment 4 (Dr. Michael Jordan - Rheumatology consultation)
(12, 4),  -- Nurse Luka Doncic assigned
(21, 4),  -- Staff Virgil van Dijk assigned
-- Appointment 5 (Dr. Michael Jordan - Pulmonology consultation)
(13, 5),  -- Nurse Karim Benzema assigned
-- Appointment 6 (Dr. Kobe Bryant - Cardiology consultation)
(14, 6),  -- Nurse Joel Embiid assigned
(22, 6),  -- Staff Russell Westbrook assigned
-- Appointment 7 (Dr. Kobe Bryant - Pulmonology consultation)
(15, 7),  -- Nurse Mohamed Salah assigned
-- Appointment 8 (Dr. Lionel Messi - Psychiatry consultation)
(9, 8),   -- Nurse Kylian Mbappé assigned
-- Appointment 9 (Dr. Lionel Messi - Ophthalmology consultation)
(10, 9),  -- Nurse Erling Haaland assigned
-- Appointment 10 (Dr. Cristiano Ronaldo - Endocrinology consultation)
(11, 10), -- Nurse Giannis Antetokounmpo assigned
-- Appointment 11 (Dr. Cristiano Ronaldo - COPD consultation)
(12, 11), -- Nurse Luka Doncic assigned
-- Appointment 12 (Dr. Stephen Curry - Rheumatology consultation)
(13, 12), -- Nurse Karim Benzema assigned
-- Appointment 13 (Dr. Stephen Curry - Routine consultation)
(14, 13); -- Nurse Joel Embiid assigned

-- Assignments for nurses appointments
INSERT INTO assignments (user_id, appointment_id) VALUES
-- Appointment 14 (Nurse Kylian Mbappé - Home visit)
(19, 14), -- Staff Paul Pogba assigned
(20, 14), -- Staff Anthony Davis assigned
-- Appointment 15 (Nurse Kylian Mbappé - Cognitive evaluation)
(21, 15), -- Staff Virgil van Dijk assigned
-- Appointment 16 (Nurse Erling Haaland - Blood pressure check)
(19, 16), -- Staff Paul Pogba assigned
-- Appointment 17 (Nurse Giannis Antetokounmpo - Pulmonary function test)
(22, 17), -- Staff Russell Westbrook assigned
-- Appointment 18 (Nurse Karim Benzema - Dialysis preparation)
(19, 18), -- Staff Paul Pogba assigned
(20, 18); -- Staff Anthony Davis assigned

-- Assignments for staff appointments
INSERT INTO assignments (user_id, appointment_id) VALUES
-- Appointment 19 (Staff Paul Pogba - Administrative support)
(9, 19),  -- Nurse Kylian Mbappé assigned for coordination
-- Appointment 20 (Staff Anthony Davis - Logistics support)
(10, 20), -- Nurse Erling Haaland assigned
-- Appointment 21 (Staff Virgil van Dijk - Social support)
(9, 21),  -- Nurse Kylian Mbappé assigned
(19, 21); -- Staff Paul Pogba assigned

-- That's it! I created all tables and inserted test data
-- 
-- To verify it works, you can do:
-- SELECT * FROM users;
-- SELECT * FROM appointments;
-- SELECT * FROM patients;
