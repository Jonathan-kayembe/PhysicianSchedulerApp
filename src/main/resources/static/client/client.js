// Medical Scheduling SaaS - Client JavaScript
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
async function login(event) {
    event.preventDefault();
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const errorDiv = document.getElementById('errorMessage');
    
    try {
        const response = await apiCall('/auth/login', 'POST', { email, password });
        
        if (response.success) {
            currentUser = response.user;
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            
            // Redirect based on role
            const roleName = response.user.role.name;
            if (roleName === 'Manager' || roleName === 'SuperAdmin') {
                window.location.href = '../manager/dashboard.html';
            } else {
                window.location.href = 'dashboard.html';
            }
        } else {
            errorDiv.textContent = response.message || 'Erreur de connexion';
        }
    } catch (error) {
        errorDiv.textContent = 'Erreur de connexion. Veuillez réessayer.';
    }
}

function logout() {
    localStorage.removeItem('currentUser');
    currentUser = null;
    window.location.href = 'login.html';
}

// ============================================
// Dashboard
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
        window.location.href = 'login.html';
    }
}

async function loadDashboardData() {
    if (!currentUser) return;
    
    try {
        const appointments = await apiCall(`/appointments?userId=${currentUser.id}`);
        const patients = await apiCall('/patients');
        
        // Calculate stats
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        
        const todayAppointments = appointments.filter(apt => {
            const aptDate = new Date(apt.appointmentTime);
            aptDate.setHours(0, 0, 0, 0);
            return aptDate.getTime() === today.getTime();
        });
        
        const weekStart = new Date(today);
        weekStart.setDate(today.getDate() - today.getDay());
        const weekEnd = new Date(weekStart);
        weekEnd.setDate(weekStart.getDate() + 7);
        
        const weekAppointments = appointments.filter(apt => {
            const aptDate = new Date(apt.appointmentTime);
            return aptDate >= weekStart && aptDate < weekEnd;
        });
        
        // Display stats
        document.getElementById('todayAppointments').textContent = todayAppointments.length;
        document.getElementById('weekAppointments').textContent = weekAppointments.length;
        document.getElementById('totalPatients').textContent = patients.length;
        
        // Display today's appointments
        displayAppointments(todayAppointments, 'todayAppointmentsList');
    } catch (error) {
        console.error('Error loading dashboard:', error);
    }
}

function displayAppointments(appointments, containerId) {
    const container = document.getElementById(containerId);
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
            <p><strong>Date:</strong> ${new Date(apt.appointmentTime).toLocaleString('fr-FR')}</p>
            <p><strong>Lieu:</strong> ${apt.location.name}</p>
            <p><strong>But:</strong> ${apt.purpose}</p>
            <p><strong>Durée:</strong> ${apt.durationMinutes} minutes</p>
            <a href="task-detail.html?id=${apt.id}" class="button">Voir détails</a>
        </div>
    `).join('');
}

// ============================================
// Appointments
// ============================================
async function loadMyAppointments() {
    if (!currentUser) return;
    
    try {
        const appointments = await apiCall(`/appointments?userId=${currentUser.id}`);
        allAppointments = appointments;
        displayMyAppointments(appointments);
    } catch (error) {
        console.error('Error loading appointments:', error);
    }
}

function displayMyAppointments(appointments) {
    const container = document.getElementById('appointmentsList');
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
            <p><strong>Date:</strong> ${new Date(apt.appointmentTime).toLocaleString('fr-FR')}</p>
            <p><strong>Lieu:</strong> ${apt.location.name}</p>
            <p><strong>But:</strong> ${apt.purpose}</p>
            <p><strong>Durée:</strong> ${apt.durationMinutes} minutes</p>
            <a href="task-detail.html?id=${apt.id}" class="button">Voir détails</a>
        </div>
    `).join('');
}

function filterAppointments() {
    const statusFilter = document.getElementById('statusFilter').value;
    const priorityFilter = document.getElementById('priorityFilter').value;
    
    let filtered = allAppointments;
    
    if (statusFilter) {
        filtered = filtered.filter(apt => apt.status === statusFilter);
    }
    
    if (priorityFilter) {
        filtered = filtered.filter(apt => apt.priority === priorityFilter);
    }
    
    displayMyAppointments(filtered);
}

function viewAppointmentDetail(appointmentId) {
    window.location.href = `task-detail.html?id=${appointmentId}`;
}

// ============================================
// Patients
// ============================================
async function loadPatients() {
    try {
        const patients = await apiCall('/patients');
        displayPatients(patients);
    } catch (error) {
        console.error('Error loading patients:', error);
    }
}

function displayPatients(patients) {
    const container = document.getElementById('patientsList');
    if (!container) return;
    
    if (patients.length === 0) {
        container.innerHTML = '<p>Aucun patient</p>';
        return;
    }
    
    container.innerHTML = patients.map(patient => `
        <div class="patient-card">
            <h3>${patient.fullName}</h3>
            <p><strong>Âge:</strong> ${patient.age} ans</p>
            <p><strong>Lieu:</strong> ${patient.location.name}</p>
            <p><strong>Notes médicales:</strong> ${patient.medicalNotes || 'Aucune'}</p>
        </div>
    `).join('');
}

// ============================================
// Appointment Details
// ============================================
async function loadAppointmentDetails(appointmentId) {
    try {
        const appointments = await apiCall(`/appointments?userId=${currentUser.id}`);
        const appointment = appointments.find(apt => apt.id === parseInt(appointmentId));
        
        if (appointment) {
            displayAppointmentDetails(appointment);
        } else {
            document.getElementById('appointmentDetails').innerHTML = '<p>Rendez-vous non trouvé</p>';
        }
    } catch (error) {
        console.error('Error loading appointment details:', error);
    }
}

function displayAppointmentDetails(appointment) {
    const container = document.getElementById('appointmentDetails');
    if (!container) return;
    
    container.innerHTML = `
        <h2>Détails du rendez-vous</h2>
        <div class="detail-row">
            <span class="detail-label">Patient:</span> ${appointment.patient.fullName}
        </div>
        <div class="detail-row">
            <span class="detail-label">Date:</span> ${new Date(appointment.appointmentTime).toLocaleString('fr-FR')}
        </div>
        <div class="detail-row">
            <span class="detail-label">Lieu:</span> ${appointment.location.name}
        </div>
        <div class="detail-row">
            <span class="detail-label">But:</span> ${appointment.purpose}
        </div>
        <div class="detail-row">
            <span class="detail-label">Durée:</span> ${appointment.durationMinutes} minutes
        </div>
        <div class="detail-row">
            <span class="detail-label">Priorité:</span> 
            <span class="priority-badge priority-${appointment.priority}">${appointment.priority}</span>
        </div>
        <div class="detail-row">
            <span class="detail-label">Statut:</span> 
            <span class="status-badge status-${appointment.status}">${appointment.status}</span>
        </div>
        <div class="detail-row">
            <span class="detail-label">Notes:</span> ${appointment.notes || 'Aucune'}
        </div>
    `;
    
    // Set current status in form
    document.getElementById('status').value = appointment.status;
}

async function updateAppointmentStatus(event) {
    event.preventDefault();
    const urlParams = new URLSearchParams(window.location.search);
    const appointmentId = urlParams.get('id');
    const status = document.getElementById('status').value;
    
    try {
        const response = await apiCall(`/appointments/${appointmentId}/status`, 'PUT', { status });
        
        if (response.success) {
            alert('Statut mis à jour avec succès');
            loadAppointmentDetails(appointmentId);
        } else {
            alert('Erreur lors de la mise à jour: ' + response.message);
        }
    } catch (error) {
        alert('Erreur lors de la mise à jour');
        console.error(error);
    }
}

