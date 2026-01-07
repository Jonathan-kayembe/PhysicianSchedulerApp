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
async function register(event) {
    event.preventDefault();
    const fullName = document.getElementById('fullName').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const roleId = parseInt(document.getElementById('roleId').value);
    const errorDiv = document.getElementById('errorMessage');
    const successDiv = document.getElementById('successMessage');
    
    // Clear previous messages and hide them
    if (errorDiv) {
        errorDiv.textContent = '';
        errorDiv.style.display = 'none';
    }
    if (successDiv) {
        successDiv.textContent = '';
        successDiv.style.display = 'none';
    }
    
    // Validate role selection
    if (!roleId || roleId === 0) {
        if (errorDiv) {
            errorDiv.textContent = 'Please select a role';
            errorDiv.style.display = 'block';
        }
        return;
    }
    
    try {
        const response = await apiCall('/auth/register', 'POST', { 
            fullName, 
            email, 
            password, 
            roleId 
        });
        
        if (response.success) {
            // Automatically log in the user after successful registration
            currentUser = response.user;
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            
            if (successDiv) {
                successDiv.textContent = 'Registration successful! Redirecting...';
                successDiv.style.display = 'block';
            }
            
            // Redirect based on role (same logic as login)
            const roleName = response.user.role.name;
            if (roleName === 'Manager' || roleName === 'SuperAdmin') {
                window.location.href = '../manager/dashboard.html';
            } else {
                window.location.href = 'dashboard.html';
            }
        } else {
            if (errorDiv) {
                errorDiv.textContent = response.message || 'Registration error';
                errorDiv.style.display = 'block';
            }
        }
    } catch (error) {
        if (errorDiv) {
            errorDiv.textContent = 'Connection error. Please try again.';
            errorDiv.style.display = 'block';
        }
    }
}

async function login(event) {
    event.preventDefault();
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const errorDiv = document.getElementById('errorMessage');
    
    // Hide error message initially
    if (errorDiv) {
        errorDiv.style.display = 'none';
        errorDiv.textContent = '';
    }
    
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
            if (errorDiv) {
                errorDiv.textContent = response.message || 'Login error';
                errorDiv.style.display = 'block';
            }
        }
    } catch (error) {
        if (errorDiv) {
            errorDiv.textContent = 'Connection error. Please try again.';
            errorDiv.style.display = 'block';
        }
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
    if (!currentUser) {
        console.error('No current user found');
        return;
    }
    
    try {
        console.log('Loading dashboard data for user:', currentUser.id, currentUser.fullName);
        
        const appointments = await apiCall(`/appointments?userId=${currentUser.id}`);
        console.log('Appointments loaded:', appointments);
        
        const patients = await apiCall('/patients');
        console.log('Patients loaded:', patients);
        
        // Calculate stats
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        
        const todayAppointments = appointments.filter(apt => {
            if (!apt || !apt.appointmentTime) return false;
            const aptDate = new Date(apt.appointmentTime);
            aptDate.setHours(0, 0, 0, 0);
            return aptDate.getTime() === today.getTime();
        });
        
        const weekStart = new Date(today);
        weekStart.setDate(today.getDate() - today.getDay());
        const weekEnd = new Date(weekStart);
        weekEnd.setDate(weekStart.getDate() + 7);
        
        const weekAppointments = appointments.filter(apt => {
            if (!apt || !apt.appointmentTime) return false;
            const aptDate = new Date(apt.appointmentTime);
            return aptDate >= weekStart && aptDate < weekEnd;
        });
        
        console.log('Today appointments:', todayAppointments.length);
        console.log('Week appointments:', weekAppointments.length);
        console.log('Total patients:', patients.length);
        
        // Display stats
        document.getElementById('todayAppointments').textContent = todayAppointments.length;
        document.getElementById('weekAppointments').textContent = weekAppointments.length;
        document.getElementById('totalPatients').textContent = patients ? patients.length : 0;
        
        // Display today's appointments
        displayAppointments(todayAppointments, 'todayAppointmentsList');
        
        // Store week appointments globally for filtering
        window.weekAppointments = weekAppointments;
        
        // Display week appointments
        displayWeekAppointments(weekAppointments);
    } catch (error) {
        console.error('Error loading dashboard:', error);
        console.error('Error details:', error.message, error.stack);
        // Afficher un message d'erreur à l'utilisateur
        const container = document.getElementById('todayAppointmentsList');
        if (container) {
            container.innerHTML = '<p class="error-message">Erreur lors du chargement des données. Vérifiez la console pour plus de détails.</p>';
        }
    }
}

function displayAppointments(appointments, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    
    if (appointments.length === 0) {
        container.innerHTML = '<p>Aucun rendez-vous</p>';
        return;
    }
    
    // Sort appointments by time
    const sortedAppointments = [...appointments].sort((a, b) => {
        return new Date(a.appointmentTime) - new Date(b.appointmentTime);
    });
    
    container.innerHTML = sortedAppointments.map(apt => `
        <div class="appointment-card">
            <h3>${apt.patient ? apt.patient.fullName : 'Patient inconnu'} 
                <span class="priority-badge priority-${apt.priority ? apt.priority.toLowerCase() : 'medium'}">${apt.priority || 'Medium'}</span>
                <span class="status-badge status-${apt.status ? apt.status.toLowerCase() : 'planned'}">${apt.status || 'Planned'}</span>
            </h3>
            <p><strong>Date:</strong> ${new Date(apt.appointmentTime).toLocaleString('fr-FR', { 
                weekday: 'long', 
                year: 'numeric', 
                month: 'long', 
                day: 'numeric', 
                hour: '2-digit', 
                minute: '2-digit' 
            })}</p>
            <p><strong>Lieu:</strong> ${apt.location ? apt.location.name : 'Non spécifié'}</p>
            <p><strong>Objectif:</strong> ${apt.purpose || 'Non spécifié'}</p>
            <p><strong>Durée:</strong> ${apt.durationMinutes || 30} minutes</p>
            ${apt.notes ? `<p><strong>Notes:</strong> ${apt.notes}</p>` : ''}
        </div>
    `).join('');
}

function displayWeekAppointments(appointments) {
    displayAppointments(appointments, 'weekAppointmentsList');
}

function filterWeekAppointments(day, buttonElement) {
    if (!window.weekAppointments) return;
    
    // Update active button
    document.querySelectorAll('.filter-btn').forEach(btn => btn.classList.remove('active'));
    if (buttonElement) {
        buttonElement.classList.add('active');
    } else {
        // Fallback: trouver le bouton correspondant
        const buttons = document.querySelectorAll('.filter-btn');
        buttons.forEach(btn => {
            const btnText = btn.textContent.toLowerCase().trim();
            if ((day === 'all' && btnText.includes('all')) ||
                (day !== 'all' && btnText.includes(day))) {
                btn.classList.add('active');
            }
        });
    }
    
    if (day === 'all') {
        displayWeekAppointments(window.weekAppointments);
        return;
    }
    
    const dayMap = {
        'monday': 1,
        'tuesday': 2,
        'wednesday': 3,
        'thursday': 4,
        'friday': 5,
        'saturday': 6,
        'sunday': 0
    };
    
    const targetDay = dayMap[day];
    if (targetDay === undefined) {
        displayWeekAppointments(window.weekAppointments);
        return;
    }
    
    const filtered = window.weekAppointments.filter(apt => {
        if (!apt || !apt.appointmentTime) return false;
        const aptDate = new Date(apt.appointmentTime);
        return aptDate.getDay() === targetDay;
    });
    
    displayWeekAppointments(filtered);
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
        container.innerHTML = '<p>No appointments</p>';
        return;
    }
    
    container.innerHTML = appointments.map(apt => `
        <div class="appointment-card">
            <h3>${apt.patient.fullName} 
                <span class="priority-badge priority-${apt.priority}">${apt.priority}</span>
                <span class="status-badge status-${apt.status}">${apt.status}</span>
            </h3>
            <p><strong>Date:</strong> ${new Date(apt.appointmentTime).toLocaleString('en-US')}</p>
            <p><strong>Location:</strong> ${apt.location.name}</p>
            <p><strong>Purpose:</strong> ${apt.purpose}</p>
            <p><strong>Duration:</strong> ${apt.durationMinutes} minutes</p>
            <a href="task-detail.html?id=${apt.id}" class="button">View details</a>
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
        container.innerHTML = '<p>No patients</p>';
        return;
    }
    
    container.innerHTML = patients.map(patient => `
        <div class="patient-card">
            <h3>${patient.fullName}</h3>
            <p><strong>Age:</strong> ${patient.age} years</p>
            <p><strong>Location:</strong> ${patient.location.name}</p>
            <p><strong>Medical notes:</strong> ${patient.medicalNotes || 'None'}</p>
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
            document.getElementById('appointmentDetails').innerHTML = '<p>Appointment not found</p>';
        }
    } catch (error) {
        console.error('Error loading appointment details:', error);
    }
}

function displayAppointmentDetails(appointment) {
    const container = document.getElementById('appointmentDetails');
    if (!container) return;
    
    container.innerHTML = `
        <h2>Appointment Details</h2>
        <div class="detail-row">
            <span class="detail-label">Patient:</span> ${appointment.patient.fullName}
        </div>
        <div class="detail-row">
            <span class="detail-label">Date:</span> ${new Date(appointment.appointmentTime).toLocaleString('en-US')}
        </div>
        <div class="detail-row">
            <span class="detail-label">Location:</span> ${appointment.location.name}
        </div>
        <div class="detail-row">
            <span class="detail-label">Purpose:</span> ${appointment.purpose}
        </div>
        <div class="detail-row">
            <span class="detail-label">Duration:</span> ${appointment.durationMinutes} minutes
        </div>
        <div class="detail-row">
            <span class="detail-label">Priority:</span> 
            <span class="priority-badge priority-${appointment.priority}">${appointment.priority}</span>
        </div>
        <div class="detail-row">
            <span class="detail-label">Status:</span> 
            <span class="status-badge status-${appointment.status}">${appointment.status}</span>
        </div>
        <div class="detail-row">
            <span class="detail-label">Notes:</span> ${appointment.notes || 'None'}
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
            alert('Status updated successfully');
            loadAppointmentDetails(appointmentId);
        } else {
            alert('Error updating status: ' + response.message);
        }
    } catch (error) {
        alert('Error updating status');
        console.error(error);
    }
}

// ============================================
// Profile
// ============================================
async function loadProfileInfo() {
    if (!currentUser) {
        window.location.href = 'login.html';
        return;
    }
    
    try {
        // Get user's appointments for statistics
        const appointments = await apiCall(`/appointments?userId=${currentUser.id}`);
        
        // Calculate statistics
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
        
        const completedAppointments = appointments.filter(apt => apt.status === 'Completed').length;
        const plannedAppointments = appointments.filter(apt => apt.status === 'Planned').length;
        
        // Display profile information
        displayProfileInfo(appointments.length, todayAppointments.length, weekAppointments.length, completedAppointments, plannedAppointments);
    } catch (error) {
        console.error('Error loading profile info:', error);
        displayProfileInfo(0, 0, 0, 0, 0);
    }
}

function displayProfileInfo(totalAppointments, todayAppointments, weekAppointments, completedAppointments, plannedAppointments) {
    const container = document.getElementById('profileInfo');
    if (!container) return;
    
    if (!currentUser) {
        container.innerHTML = '<p>User information not available</p>';
        return;
    }
    
    container.innerHTML = `
        <div class="profile-card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                <h2>Personal Information</h2>
                <button onclick="showEditForm()" class="button">Edit Profile</button>
            </div>
            <div class="detail-row">
                <span class="detail-label">Full Name:</span> ${currentUser.fullName || 'N/A'}
            </div>
            <div class="detail-row">
                <span class="detail-label">Email:</span> ${currentUser.email || 'N/A'}
            </div>
            <div class="detail-row">
                <span class="detail-label">Role:</span> 
                <span class="role-badge">${currentUser.role ? currentUser.role.name : 'N/A'}</span>
            </div>
            <div class="detail-row">
                <span class="detail-label">User ID:</span> ${currentUser.id || 'N/A'}
            </div>
        </div>
        
        <div class="profile-card" style="margin-top: 20px;">
            <h2>Statistics</h2>
            <div class="stats-grid">
                <div class="stat-item">
                    <span class="stat-label">Total Appointments:</span>
                    <span class="stat-value">${totalAppointments}</span>
                </div>
                <div class="stat-item">
                    <span class="stat-label">Today's Appointments:</span>
                    <span class="stat-value">${todayAppointments}</span>
                </div>
                <div class="stat-item">
                    <span class="stat-label">This Week's Appointments:</span>
                    <span class="stat-value">${weekAppointments}</span>
                </div>
                <div class="stat-item">
                    <span class="stat-label">Completed:</span>
                    <span class="stat-value">${completedAppointments}</span>
                </div>
                <div class="stat-item">
                    <span class="stat-label">Planned:</span>
                    <span class="stat-value">${plannedAppointments}</span>
                </div>
            </div>
        </div>
    `;
}

function showEditForm() {
    if (!currentUser) return;
    
    // Populate form with current values
    document.getElementById('editFullName').value = currentUser.fullName || '';
    document.getElementById('editEmail').value = currentUser.email || '';
    document.getElementById('editRole').value = currentUser.role ? currentUser.role.name : 'N/A';
    
    // Show edit form and hide profile info
    document.getElementById('editProfileSection').style.display = 'block';
    document.getElementById('profileInfo').style.display = 'none';
    
    // Clear any previous messages
    document.getElementById('profileMessage').innerHTML = '';
}

function cancelEdit() {
    // Hide edit form and show profile info
    document.getElementById('editProfileSection').style.display = 'none';
    document.getElementById('profileInfo').style.display = 'block';
    document.getElementById('profileMessage').innerHTML = '';
}

async function updateProfile(event) {
    event.preventDefault();
    if (!currentUser) return;
    
    const fullName = document.getElementById('editFullName').value.trim();
    const email = document.getElementById('editEmail').value.trim();
    const messageDiv = document.getElementById('profileMessage');
    
    // Validation
    if (!fullName || fullName.length < 2) {
        messageDiv.innerHTML = '<div class="error-message">Full name must be at least 2 characters</div>';
        return;
    }
    
    if (!email) {
        messageDiv.innerHTML = '<div class="error-message">Email is required</div>';
        return;
    }
    
    try {
        const response = await apiCall(`/users/${currentUser.id}`, 'PUT', {
            fullName: fullName,
            email: email
        });
        
        if (response.success) {
            // Update current user in localStorage
            currentUser.fullName = response.user.fullName;
            currentUser.email = response.user.email;
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            
            // Update displayed name in header
            const userNameEl = document.getElementById('userName');
            if (userNameEl) {
                userNameEl.textContent = currentUser.fullName;
            }
            
            messageDiv.innerHTML = '<div class="success-message">Profile updated successfully!</div>';
            
            // Reload profile info after 1 second
            setTimeout(() => {
                cancelEdit();
                loadProfileInfo();
            }, 1500);
        } else {
            messageDiv.innerHTML = '<div class="error-message">' + (response.message || 'Error updating profile') + '</div>';
        }
    } catch (error) {
        messageDiv.innerHTML = '<div class="error-message">Error updating profile. Please try again.</div>';
        console.error('Error updating profile:', error);
    }
}
