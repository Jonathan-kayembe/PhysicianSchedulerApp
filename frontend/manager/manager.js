// Medical Scheduling SaaS - Manager JavaScript
// API Base URL
const API_BASE_URL = 'http://localhost:8080';

// Store current user
let currentUser = null;

// ============================================
// API Call Function
// ============================================
async function apiCall(endpoint, method = 'GET', data = null) {
    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        }
    };
    
    if (data) {
        options.body = JSON.stringify(data);
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
        const result = await response.json();
        return result;
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
}

// ============================================
// Authentication
// ============================================
function loadUserInfo() {
    const userStr = localStorage.getItem('currentUser');
    if (userStr) {
        currentUser = JSON.parse(userStr);
        const userNameEl = document.getElementById('userName');
        if (userNameEl) {
            userNameEl.textContent = currentUser.fullName;
        }
    } else {
        window.location.href = '../client/login.html';
    }
}

function logout() {
    localStorage.removeItem('currentUser');
    currentUser = null;
    window.location.href = '../client/login.html';
}

// ============================================
// Dashboard
// ============================================
async function loadDashboardStats() {
    try {
        const users = await apiCall('/users');
        const patients = await apiCall('/patients');
        const locations = await apiCall('/locations');
        
        // Get all appointments (we'll need to get them from all users)
        let allAppointments = [];
        for (const user of users) {
            try {
                const userAppointments = await apiCall(`/appointments?userId=${user.id}`);
                allAppointments = allAppointments.concat(userAppointments);
            } catch (error) {
                console.error(`Error loading appointments for user ${user.id}:`, error);
            }
        }
        
        // Display stats
        document.getElementById('totalUsers').textContent = users.length;
        document.getElementById('totalPatients').textContent = patients.length;
        document.getElementById('totalAppointments').textContent = allAppointments.length;
        document.getElementById('totalLocations').textContent = locations.length;
        
        // Display recent appointments (last 10)
        const recentAppointments = allAppointments
            .sort((a, b) => new Date(b.appointmentTime) - new Date(a.appointmentTime))
            .slice(0, 10);
        displayRecentAppointments(recentAppointments);
    } catch (error) {
        console.error('Error loading dashboard stats:', error);
    }
}

function displayRecentAppointments(appointments) {
    const container = document.getElementById('recentAppointments');
    if (!container) return;
    
    if (appointments.length === 0) {
        container.innerHTML = '<p>Aucun rendez-vous</p>';
        return;
    }
    
    container.innerHTML = appointments.map(apt => `
        <div class="appointment-card">
            <h3>${apt.patient.fullName} 
                <span class="priority-badge priority-${apt.priority}">${apt.priority}</span>
                <span class="status-badge status-${apt.status}">${apt.status}</span>
            </h3>
            <p><strong>Utilisateur:</strong> ${apt.user.fullName}</p>
            <p><strong>Date:</strong> ${new Date(apt.appointmentTime).toLocaleString('fr-FR')}</p>
            <p><strong>Lieu:</strong> ${apt.location.name}</p>
            <p><strong>But:</strong> ${apt.purpose}</p>
        </div>
    `).join('');
}

// ============================================
// Users
// ============================================
async function loadUsers() {
    try {
        const users = await apiCall('/users');
        displayUsers(users);
    } catch (error) {
        console.error('Error loading users:', error);
    }
}

function displayUsers(users) {
    const container = document.getElementById('usersList');
    if (!container) return;
    
    if (users.length === 0) {
        container.innerHTML = '<p>Aucun utilisateur</p>';
        return;
    }
    
    container.innerHTML = users.map(user => `
        <div class="user-card">
            <h3>${user.fullName}</h3>
            <p><strong>Email:</strong> ${user.email}</p>
            <p><strong>Rôle:</strong> ${user.role.name}</p>
        </div>
    `).join('');
}

function showAddUserForm() {
    document.getElementById('addUserForm').style.display = 'block';
}

function hideAddUserForm() {
    document.getElementById('addUserForm').style.display = 'none';
    document.getElementById('userForm').reset();
}

async function addUser(event) {
    event.preventDefault();
    const fullName = document.getElementById('fullName').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const roleId = parseInt(document.getElementById('roleId').value);
    
    try {
        const response = await apiCall('/auth/register', 'POST', {
            fullName,
            email,
            password,
            roleId
        });
        
        if (response.success) {
            alert('Utilisateur créé avec succès');
            hideAddUserForm();
            loadUsers();
        } else {
            alert('Erreur: ' + response.message);
        }
    } catch (error) {
        alert('Erreur lors de la création de l\'utilisateur');
        console.error(error);
    }
}

// ============================================
// Locations
// ============================================
async function loadLocations() {
    try {
        const locations = await apiCall('/locations');
        displayLocations(locations);
    } catch (error) {
        console.error('Error loading locations:', error);
    }
}

function displayLocations(locations) {
    const container = document.getElementById('locationsList');
    if (!container) return;
    
    if (locations.length === 0) {
        container.innerHTML = '<p>Aucun lieu</p>';
        return;
    }
    
    container.innerHTML = locations.map(location => `
        <div class="location-card">
            <h3>${location.name}</h3>
            <p><strong>Type:</strong> ${location.type}</p>
            <p><strong>Heures:</strong> ${location.openingHour} - ${location.closingHour}</p>
            <p><strong>Capacité/jour:</strong> ${location.capacityPerDay}</p>
        </div>
    `).join('');
}

function showAddLocationForm() {
    document.getElementById('addLocationForm').style.display = 'block';
}

function hideAddLocationForm() {
    document.getElementById('addLocationForm').style.display = 'none';
    document.getElementById('locationForm').reset();
}

async function addLocation(event) {
    event.preventDefault();
    const name = document.getElementById('name').value;
    const type = document.getElementById('type').value;
    const openingHour = document.getElementById('openingHour').value + ':00';
    const closingHour = document.getElementById('closingHour').value + ':00';
    const capacityPerDay = parseInt(document.getElementById('capacityPerDay').value);
    
    try {
        const response = await apiCall('/locations', 'POST', {
            name,
            type,
            openingHour,
            closingHour,
            capacityPerDay
        });
        
        if (response.success) {
            alert('Lieu créé avec succès');
            hideAddLocationForm();
            loadLocations();
        } else {
            alert('Erreur: ' + response.message);
        }
    } catch (error) {
        alert('Erreur lors de la création du lieu');
        console.error(error);
    }
}

// ============================================
// Appointments
// ============================================
async function loadUsersForAppointment() {
    try {
        const users = await apiCall('/users');
        const select = document.getElementById('userId');
        select.innerHTML = users.map(user => 
            `<option value="${user.id}">${user.fullName} (${user.role.name})</option>`
        ).join('');
    } catch (error) {
        console.error('Error loading users:', error);
    }
}

async function loadPatientsForAppointment() {
    try {
        const patients = await apiCall('/patients');
        const select = document.getElementById('patientId');
        select.innerHTML = patients.map(patient => 
            `<option value="${patient.id}">${patient.fullName} (${patient.location.name})</option>`
        ).join('');
    } catch (error) {
        console.error('Error loading patients:', error);
    }
}

async function loadLocationsForAppointment() {
    try {
        const locations = await apiCall('/locations');
        const select = document.getElementById('locationId');
        select.innerHTML = locations.map(location => 
            `<option value="${location.id}">${location.name} (${location.type})</option>`
        ).join('');
    } catch (error) {
        console.error('Error loading locations:', error);
    }
}

async function createAppointment(event) {
    event.preventDefault();
    const userId = parseInt(document.getElementById('userId').value);
    const patientId = parseInt(document.getElementById('patientId').value);
    const locationId = parseInt(document.getElementById('locationId').value);
    const purpose = document.getElementById('purpose').value;
    const durationMinutes = parseInt(document.getElementById('durationMinutes').value);
    const priority = document.getElementById('priority').value;
    const status = document.getElementById('status').value;
    const appointmentTime = document.getElementById('appointmentTime').value;
    const notes = document.getElementById('notes').value;
    
    // Convert datetime-local to ISO format
    const appointmentDateTime = new Date(appointmentTime).toISOString().slice(0, 19);
    
    const warningDiv = document.getElementById('warningMessage');
    warningDiv.innerHTML = '';
    
    try {
        const response = await apiCall('/appointments', 'POST', {
            userId,
            patientId,
            locationId,
            purpose,
            durationMinutes,
            priority,
            status,
            appointmentTime: appointmentDateTime,
            notes
        });
        
        if (response.success) {
            if (response.warning) {
                warningDiv.innerHTML = `<div class="warning-message">${response.warning}</div>`;
            } else {
                alert('Rendez-vous créé avec succès');
                document.getElementById('appointmentForm').reset();
            }
        } else {
            alert('Erreur: ' + response.message);
        }
    } catch (error) {
        alert('Erreur lors de la création du rendez-vous');
        console.error(error);
    }
}

// ============================================
// Analytics
// ============================================
async function loadAnalytics() {
    try {
        const users = await apiCall('/users');
        const patients = await apiCall('/patients');
        const locations = await apiCall('/locations');
        
        // Get all appointments
        let allAppointments = [];
        for (const user of users) {
            try {
                const userAppointments = await apiCall(`/appointments?userId=${user.id}`);
                allAppointments = allAppointments.concat(userAppointments);
            } catch (error) {
                console.error(`Error loading appointments for user ${user.id}:`, error);
            }
        }
        
        // Calculate analytics
        const appointmentsByStatus = {};
        const appointmentsByPriority = {};
        const appointmentsByLocation = {};
        
        allAppointments.forEach(apt => {
            // By status
            appointmentsByStatus[apt.status] = (appointmentsByStatus[apt.status] || 0) + 1;
            
            // By priority
            appointmentsByPriority[apt.priority] = (appointmentsByPriority[apt.priority] || 0) + 1;
            
            // By location
            const locationName = apt.location.name;
            appointmentsByLocation[locationName] = (appointmentsByLocation[locationName] || 0) + 1;
        });
        
        // Display analytics
        const container = document.getElementById('analyticsContent');
        container.innerHTML = `
            <div class="analytics-item">
                <h3>Répartition par statut</h3>
                ${Object.entries(appointmentsByStatus).map(([status, count]) => 
                    `<p>${status}: ${count}</p>`
                ).join('')}
            </div>
            <div class="analytics-item">
                <h3>Répartition par priorité</h3>
                ${Object.entries(appointmentsByPriority).map(([priority, count]) => 
                    `<p>${priority}: ${count}</p>`
                ).join('')}
            </div>
            <div class="analytics-item">
                <h3>Répartition par lieu</h3>
                ${Object.entries(appointmentsByLocation).map(([location, count]) => 
                    `<p>${location}: ${count}</p>`
                ).join('')}
            </div>
            <div class="analytics-item">
                <h3>Statistiques générales</h3>
                <p>Total utilisateurs: ${users.length}</p>
                <p>Total patients: ${patients.length}</p>
                <p>Total lieux: ${locations.length}</p>
                <p>Total rendez-vous: ${allAppointments.length}</p>
            </div>
        `;
    } catch (error) {
        console.error('Error loading analytics:', error);
        document.getElementById('analyticsContent').innerHTML = 
            '<p>Erreur lors du chargement des données analytiques</p>';
    }
}

