# API Testing Guide

## Quick Test Commands (PowerShell)

### 1. Start the Backend

```powershell
cd backend
mvn spring-boot:run
```

Wait for "Started BackendApplication" message.

---

## Test User Registration

```powershell
$body = @{
    name = "John Doe"
    email = "john@example.com"
    password = "password123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" -Method Post -Body $body -ContentType "application/json"
```

**Expected Response:**

```json
{
  "success": true,
  "message": "User registered successfully",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

---

## Test User Login

```powershell
$body = @{
    email = "john@example.com"
    password = "password123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/users/login" -Method Post -Body $body -ContentType "application/json"
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Login successful",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

---

## Test Create Event

```powershell
$body = @{
    userId = 1
    title = "Team Meeting"
    description = "Weekly team sync"
    startDateTime = "2026-01-15T10:00:00"
    endDateTime = "2026-01-15T11:00:00"
    category = "PROFESSIONAL"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/events" -Method Post -Body $body -ContentType "application/json"
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Event created successfully",
  "event": {
    "id": 1,
    "userId": 1,
    "title": "Team Meeting",
    "description": "Weekly team sync",
    "startDateTime": "2026-01-15T10:00:00",
    "endDateTime": "2026-01-15T11:00:00",
    "category": "PROFESSIONAL"
  }
}
```

---

## Test Get All Events

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/events" -Method Get
```

---

## Test Get Events by User

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/events/user/1" -Method Get
```

---

## Test Update Event

```powershell
$body = @{
    userId = 1
    title = "Team Meeting - Updated"
    description = "Weekly team sync - Changed time"
    startDateTime = "2026-01-15T14:00:00"
    endDateTime = "2026-01-15T15:00:00"
    category = "PROFESSIONAL"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/events/1" -Method Put -Body $body -ContentType "application/json"
```

---

## Test Create Backup

```powershell
$body = @{
    backupName = "my_test_backup"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/backup/create" -Method Post -Body $body -ContentType "application/json"
```

---

## Test List Backups

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/backup/list" -Method Get
```

---

## Test Check Email Exists

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/users/check-email?email=john@example.com" -Method Get
```

---

## Test Delete Event

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/events/1" -Method Delete
```

---

## Alternative: Using curl (if available)

### Register User

```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"password\":\"password123\"}"
```

### Create Event

```bash
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d "{\"userId\":1,\"title\":\"Meeting\",\"description\":\"Test\",\"startDateTime\":\"2026-01-15T10:00:00\",\"endDateTime\":\"2026-01-15T11:00:00\",\"category\":\"PROFESSIONAL\"}"
```

---

## Checking Data Files

After creating users and events, you can verify the data:

```powershell
# View users
Get-Content backend\csvFiles\users.csv

# View events
Get-Content backend\csvFiles\events.csv

# View backups
Get-ChildItem backend\backups\
```

---

## Common Issues

### Port Already in Use

If port 8080 is busy:

1. Stop any running Java processes
2. Or change port in `application.properties`: `server.port=8081`

### CSV Files Not Created

- Ensure you have write permissions in the backend directory
- The app automatically creates the directories and CSV files

### CORS Errors

- The CORS configuration allows all origins for development
- For production, update allowed origins in `config.java`
