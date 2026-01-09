# Quick Start Guide

## 🚀 Start Your Application

### 1️⃣ Start Backend (Required First!)

```powershell
cd c:\Users\junto\Documents\y1s1\calendar_app\backend
mvn spring-boot:run
```

**Wait for:** `Started BackendApplication` message

---

### 2️⃣ Start Frontend

Open a NEW terminal:

```powershell
cd c:\Users\junto\Documents\y1s1\calendar_app\frontend
mvn clean javafx:run
```

---

## 🧪 Quick Test (Optional)

Test if backend is running:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/users" -Method Get
```

Create a test user:

```powershell
$user = @{name="Test User";email="test@test.com";password="pass123"} | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" -Method Post -Body $user -ContentType "application/json"
```

---

## 📁 What Was Implemented

### ✅ Backend REST API

- **UserController** - Registration, login, email checking
- **EventController** - Full CRUD for events
- **BackupController** - Create/restore/list/delete backups
- **RecurrentController** - Manage recurring event rules
- **CORS Config** - Cross-origin request support

### ✅ API Endpoints (20 total)

**Users:**

- POST `/api/users/register`
- POST `/api/users/login`
- GET `/api/users`
- GET `/api/users/check-email?email={email}`

**Events:**

- GET `/api/events`
- GET `/api/events/{id}`
- GET `/api/events/user/{userId}`
- GET `/api/events/category/{category}`
- POST `/api/events`
- PUT `/api/events/{id}`
- DELETE `/api/events/{id}`

**Recurring:**

- GET `/api/recurrent`
- GET `/api/recurrent/{eventId}`
- POST `/api/recurrent`
- PUT `/api/recurrent/{eventId}`
- DELETE `/api/recurrent/{eventId}`

**Backup:**

- POST `/api/backup/create`
- POST `/api/backup/restore`
- GET `/api/backup/list`
- DELETE `/api/backup/{backupName}`

---

## 📊 Data Storage

CSV files created automatically:

- `backend/csvFiles/users.csv` - User accounts
- `backend/csvFiles/events.csv` - Calendar events
- `backend/csvFiles/recurrent.csv` - Recurring rules
- `backend/backups/` - Backup files

---

## 🎨 Event Categories

- PROFESSIONAL (Brown)
- PERSONAL (Green)
- HEALTH (Pink)
- EDUCATION (Purple)
- SOCIAL (Orange)
- FINANCE (Blue Grey)
- HOLIDAY (Red)
- OTHER (Grey)

---

## 🔧 Configuration

- **Backend Port:** 8080
- **Java Version:** 25 (backend), 21 (frontend)
- **Framework:** Spring Boot 4.0.0, JavaFX 21

---

## 📖 More Help

- **Full API Tests:** See `API_TESTING_GUIDE.md`
- **Complete Details:** See `IMPLEMENTATION_SUMMARY.md`
- **Project Info:** See `README.md`

---

## ⚠️ Common Issues

**"Port 8080 already in use"**

```powershell
# Find and kill the process
Get-Process -Name "java" | Stop-Process -Force
```

**"Backend not responding"**

- Make sure backend is running first
- Check console for "Started BackendApplication"
- Verify: http://localhost:8080/api/users

**"CSV files not found"**

- Files are created automatically on first run
- Check `backend/csvFiles/` directory

---

## ✨ You're All Set!

Your calendar application backend is fully implemented and ready to use. All REST controllers are connected to your existing services, and the frontend can now communicate with the backend API.

**Status:** ✅ READY TO RUN
