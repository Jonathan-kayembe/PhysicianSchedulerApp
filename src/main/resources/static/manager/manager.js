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
        container.innerHTML = '<p>No appointments</p>';
        return;
    }
    
    container.innerHTML = appointments.map(apt => `
        <div class="appointment-card">
            <h3>${apt.patient.fullName} 
                <span class="priority-badge priority-${apt.priority}">${apt.priority}</span>
                <span class="status-badge status-${apt.status}">${apt.status}</span>
            </h3>
            <p><strong>User:</strong> ${apt.user.fullName}</p>
            <p><strong>Date:</strong> ${new Date(apt.appointmentTime).toLocaleString('en-US')}</p>
            <p><strong>Location:</strong> ${apt.location.name}</p>
            <p><strong>Purpose:</strong> ${apt.purpose}</p>
        </div>
    `).join('');
}

// ============================================
// Users
// ============================================
async function loadUsers() {
    try {
        console.log('Loading users from database...');
        const users = await apiCall('/users');
        console.log('Users loaded:', users.length, 'users found');
        console.log('Users data:', users);
        displayUsers(users);
    } catch (error) {
        console.error('Error loading users:', error);
        const container = document.getElementById('usersList');
        if (container) {
            container.innerHTML = `<p style="color: red;">Error loading users: ${error.message}</p>`;
        }
    }
}

function displayUsers(users) {
    const container = document.getElementById('usersList');
    if (!container) return;
    
    if (users.length === 0) {
        container.innerHTML = '<p>No users found in database</p>';
        return;
    }
    
    // Sort users by ID (newest first)
    users.sort((a, b) => (b.id || 0) - (a.id || 0));
    
    container.innerHTML = `
        <div style="margin-bottom: 20px; padding: 10px; background-color: #f0f0f0; border-radius: 5px;">
            <strong>Total Users: ${users.length}</strong>
        </div>
        ${users.map(user => `
        <div class="user-card">
            <h3>${user.fullName || 'N/A'}</h3>
            <p><strong>ID:</strong> ${user.id || 'N/A'}</p>
            <p><strong>Email:</strong> ${user.email || 'N/A'}</p>
            <p><strong>Role:</strong> ${user.role ? user.role.name : 'N/A'}</p>
            <p><strong>Role ID:</strong> ${user.role ? user.role.id : 'N/A'}</p>
        </div>
    `).join('')}
    `;
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
            alert('User created successfully');
            hideAddUserForm();
            loadUsers();
        } else {
            alert('Error: ' + response.message);
        }
    } catch (error) {
        alert('Error creating user');
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
        container.innerHTML = '<p>No locations</p>';
        return;
    }
    
    container.innerHTML = locations.map(location => `
        <div class="location-card">
            <h3>${location.name}</h3>
            <p><strong>Type:</strong> ${location.type}</p>
            <p><strong>Hours:</strong> ${location.openingHour} - ${location.closingHour}</p>
            <p><strong>Daily Capacity:</strong> ${location.capacityPerDay}</p>
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
            alert('Location created successfully');
            hideAddLocationForm();
            loadLocations();
        } else {
            alert('Error: ' + response.message);
        }
    } catch (error) {
        alert('Error creating location');
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
                alert('Appointment created successfully');
                document.getElementById('appointmentForm').reset();
            }
        } else {
            alert('Error: ' + response.message);
        }
    } catch (error) {
        alert('Error creating appointment');
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
                <h3>Distribution by Status</h3>
                ${Object.entries(appointmentsByStatus).map(([status, count]) => 
                    `<p>${status}: ${count}</p>`
                ).join('')}
            </div>
            <div class="analytics-item">
                <h3>Distribution by Priority</h3>
                ${Object.entries(appointmentsByPriority).map(([priority, count]) => 
                    `<p>${priority}: ${count}</p>`
                ).join('')}
            </div>
            <div class="analytics-item">
                <h3>Distribution by Location</h3>
                ${Object.entries(appointmentsByLocation).map(([location, count]) => 
                    `<p>${location}: ${count}</p>`
                ).join('')}
            </div>
            <div class="analytics-item">
                <h3>General Statistics</h3>
                <p>Total Users: ${users.length}</p>
                <p>Total Patients: ${patients.length}</p>
                <p>Total Locations: ${locations.length}</p>
                <p>Total Appointments: ${allAppointments.length}</p>
            </div>
        `;
    } catch (error) {
        console.error('Error loading analytics:', error);
        document.getElementById('analyticsContent').innerHTML = 
            '<p>Error loading analytics data</p>';
    }
}
