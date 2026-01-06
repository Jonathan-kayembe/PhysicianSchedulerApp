# Medical Scheduling SaaS Application

Complete SaaS medical scheduling application with appointment and availability management.

## 🏗️ Project Structure

```
Projet_UA3_Final/
├── database_complete_schema.sql    # Complete database schema with sample data
├── backend/                         # Spring Boot Backend
│   ├── pom.xml                      # Maven dependencies
│   └── src/main/
│       ├── java/com/saas/medicalapp/
│       │   ├── MedicalAppApplication.java    # Spring Boot main class
│       │   ├── config/
│       │   │   └── CorsConfig.java          # CORS configuration
│       │   ├── model/                       # JPA entities
│       │   ├── repository/                  # JPA repositories
│       │   ├── service/                     # Business logic
│       │   └── controller/                  # REST controllers
│       └── resources/
│           └── application.properties       # Configuration
├── frontend/
│   ├── client/                             # Client Dashboard (Physicians, Nurses, Staff)
│   │   ├── login.html
│   │   ├── dashboard.html
│   │   ├── appointments.html
│   │   ├── patients.html
│   │   ├── task-detail.html
│   │   ├── style.css
│   │   └── client.js
│   └── manager/                            # Manager Panel (Managers, Admins)
│       ├── dashboard.html
│       ├── users.html
│       ├── locations.html
│       ├── create-appointment.html
│       ├── analytics.html
│       ├── style.css
│       └── manager.js
└── README.md
```

## 🛠️ Technologies Used

### Backend
- **Java 17**
- **Spring Boot 3.1.5**
- **Spring Data JPA** (database access)
- **MySQL Connector** (database driver)
- **Maven** (dependency management)

### Frontend
- **HTML5**
- **CSS3** (beige theme, simplified design)
- **Vanilla JavaScript** (no frameworks)
- **Fetch API** (API calls)

### Database
- **MySQL** or **MariaDB**
- **SQL** (schema definition and queries)

## 📊 Database Schema

The project uses 7 main tables:
1. **roles** - User roles
2. **users** - System users
3. **locations** - Medical locations
4. **availability** - User availability
5. **patients** - Patients
6. **appointments** - Appointments
7. **assignments** - Assignments (junction table)

See `database_complete_schema.sql` for the complete schema.

## 🚀 Installation and Configuration

### 1. Database

1. Create a MySQL database named `medicaldb`:
```sql
CREATE DATABASE medicaldb;
```

2. Run the SQL script:
```bash
mysql -u root -p medicaldb < database_complete_schema.sql
```

### 2. Backend Configuration

Modify `src/main/resources/application.properties` if necessary:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/medicaldb?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. Compilation and Execution

```bash
# Compile the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The backend will be accessible at `http://localhost:8080`

### 4. Frontend

Open the HTML files directly in a browser or use a local server.

**Note:** To avoid CORS issues, open the HTML files via a local HTTP server (for example with Live Server in VS Code, or Python `python -m http.server`).

## 🔑 Test Accounts

See `database_complete_schema.sql` for the complete list.

**Examples:**
- Physician: `lebron.james@medical.com` / `lebron2024`
- Nurse: `kylian.mbappe@medical.com` / `mbappe10`
- Manager: `zlatan.ibrahimovic@medical.com` / `zlatan10`
- Admin: `admin@medical.com` / `admin123`

## 🔌 API Endpoints

### Base URL: `http://localhost:8080`

- **POST** `/auth/register` - Registration
- **POST** `/auth/login` - Login
- **GET** `/auth/logout` - Logout
- **GET** `/users` - List users
- **GET** `/locations` - List locations
- **GET** `/availability?userId=1` - User availability
- **GET** `/patients` - List patients
- **GET** `/appointments?userId=1` - User appointments
- **POST** `/appointments` - Create appointment
- **PUT** `/appointments/{id}/status` - Update status
- **GET** `/assignments?userId=1` - User assignments

See the complete documentation in the project specification file.

## 🎨 User Interface

### Client Dashboard
- Login
- Dashboard with statistics
- Appointment list with filters
- Patient list
- Appointment details and updates

### Manager Panel
- Dashboard with global statistics
- User management
- Location management
- Appointment creation with overbooking verification
- Analytics and statistics

## ⚠️ Key Features

### Overbooking Verification
The system automatically checks if a new appointment exceeds a user's availability and displays a warning.

### Role Management
- **Physician** - Doctors
- **Nurse** - Nurses
- **Staff** - Staff members
- **Manager** - Managers
- **SuperAdmin** - Administrators

Users are redirected to the appropriate interface based on their role.

## 📝 Important Notes

- Passwords are **hashed** with BCrypt in this version
- CORS is configured for `http://localhost:63342` (PhpStorm/WebStorm) and `http://localhost:8080`
- The beige theme is used for a simple and professional design
- All endpoints return JSON

## 🐛 Troubleshooting

### CORS Issues
If you encounter CORS errors, check that:
1. The backend is running on port 8080
2. The allowed origins in `CorsConfig.java` match your frontend URL
3. You are using a local HTTP server to serve the HTML files

### Database Issues
1. Check that MySQL is running
2. Check the credentials in `application.properties`
3. Check that the `medicaldb` database exists

## 📚 Documentation

For more details on the project structure, API endpoints, and business logic, consult the complete documentation provided in the project specifications.

---

**Developed with Spring Boot 3.1.5 and Java 17**
