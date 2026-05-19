# 🎯 TMWS - Task Management Web System

## Projektstatus:

Dieses Projekt ist eine **vollständig funktionale, moderne Kanban-basierte Aufgabenmanagement-Anwendung** mit:

- 🔐 JWT-basierte Authentifizierung (Login, Registrierung, Account Management)
- 📊 Kanban-Board mit Spalten und Aufgaben (Drag & Drop)
- 👥 Mehrbenutzer-Unterstützung mit Profilverwaltung
- 🌍 100% Deutsche UI
- ⚡ Echtzeit-Validierung mit visuellen Fehlern
- 💾 Persistierte Speicherung (H2 Database)

---

## 🚀 Schnellstart (2 Minuten)

### 1. Backend starten
```bash
cd backend
.\gradlew.bat bootRun
```
**Läuft auf**: http://localhost:8080

### 2. Frontend starten (neues Terminal)
```bash
cd frontend
pnpm install
pnpm start
```
**Läuft auf**: http://localhost:4200

### 3. Öffne im Browser
```
http://localhost:4200/login
```

### 4. Anmelden
```
Email: admin@tmws.local
Passwort: Admin@123456789
```

---

## 📋 Features

### ✅ Authentication
- [x] Login mit JWT Token
- [x] Registrierung mit E-Mail-Verifikation
- [x] Passwort Reset via E-Mail
- [x] Sichere Passwort-Speicherung (BCrypt)
- [x] Session-Management

### ✅ Dashboard
- [x] Kanban-Board mit 3 Spalten (Standard)
- [x] Aufgaben erstellen, bearbeiten, löschen
- [x] Drag & Drop zwischen Spalten
- [x] Neue Spalten hinzufügen, berarbeiten, löschen
- [x] Suchleiste mit Live-Vorschlägen
- [x] Profil-Avatar mit Initialen

### ✅ Account Management
- [x] Profil anzeigen
- [x] Namen ändern
- [x] Passwort ändern
- [x] Account deaktivieren
- [x] Abmelden

### ✅ Sicherheit
- [x] JWT Bearer Token Authentication
- [x] CORS-Protection (nur localhost:4200)
- [x] CSRF Disabled (REST-API)
- [x] Passwort-Hashing (BCrypt)
- [x] Email-Validierung
- [x] Token Expiration

---

## 🏗️ Architektur

```
┌───────────────────────────────────────────────────────────────┐
│ Frontend (Angular)                                            │
│ ├── Dashboard Component                                       │
│ ├── Auth Service (JWT Token Management)                       │
│ ├── Board Service (API Client)                                │
│ └── Profile Popup (Account Management)                        │
└──────────────────────┬────────────────────────────────────────┘
                       │ (REST API, Bearer Token)
                       ▼
┌───────────────────────────────────────────────────────────────┐
│ Backend (Java Spring Boot/Spring Security)                    │
│ ├── AuthController (/api/auth/*)                              │
│ ├── BoardController (/api/boards/*)                           │
│ ├── JwtAuthenticationFilter                                   │
│ ├── AuthenticationService (Login, Register, Profile)          │
│ ├── BoardService (Board, Task, Column Management)             │
│ └── SecurityConfig (Java Spring Security + JWT)               │
└──────────────────────┬────────────────────────────────────────┘
                       │ (JPA/Repository Access)
                       ▼
┌───────────────────────────────────────────────────────────────┐
│ Database (H2 In-Memory)                                       │
│ ├── users (id, name, email, password, enabled)                │
│ ├── boards (id, title, background, owner_id)                  │
│ ├── board_columns (id, title, position, board_id)             │
│ ├── tasks (id, title, description, deadline, column_id)       │
│ ├── task_assignees (junction table)                           │
│ ├── verification_tokens                                       │
│ └── password_reset_tokens                                     │
└───────────────────────────────────────────────────────────────┘
```

---

## 📚 Dokumentation

| Dokument                               | Beschreibung                    |
|----------------------------------------|---------------------------------|
| [README.md](./README.md)               | Projekt-Übersicht               |
| [SETUP.md](./SETUP.md)                 | Initiale Einrichtung            |
| [MAILPIT_SETUP.md](./MAILPIT_SETUP.md)       | Mailpit Konfiguration & Nutzung |
| [CONFIGURATION.md](./CONFIGURATION.md) | Konfigurationsübersicht         |

---

## 🔌 API-Endpunkte

### Authentication
```
POST   /api/auth/login                    (no token required)
POST   /api/auth/register                 (no token required)
POST   /api/auth/password-reset           (no token required)
GET    /api/auth/verify-email             (no token required)
PUT    /api/auth/reset-password           (no token required)
GET    /api/auth/health                   (no token required)
```

### Account Management
```
GET    /api/auth/me?email=email              (token required)
PUT    /api/auth/profile?email=email         (token required)
PUT    /api/auth/change-password?email=email (token required)
DELETE /api/auth/me?email=email              (token required)
```

### Boards
```
GET    /api/boards                          (token required)
GET    /api/boards/{id}                     (token required)
POST   /api/boards?ownerEmail=email         (token required)
PUT    /api/boards/{id}                     (token required)
POST   /api/boards/{id}/invite?email=email  (token required)
POST   /api/boards/{id}/columns?title=title (token required)
```

### Tasks
```
POST   /api/boards/columns/{columnId}/tasks  (token required)
PUT    /api/boards/tasks/{taskId}            (token required)
PUT    /api/boards/tasks/{taskId}/move?...   (token required)
DELETE /api/boards/tasks/{taskId}            (token required)
```

---

## 🧪 Testing

### Backend Tests (PowerShell)
```powershell
# Login
$login = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
  -Method POST -Body '{"email":"admin@tmws.local","password":"Admin@123456789"}' `
  -ContentType "application/json" -UseBasicParsing | Select-Object -ExpandProperty Content | ConvertFrom-Json

$token = $login.token

# Get Profile
Invoke-WebRequest -Uri "http://localhost:8080/api/auth/me?email=admin%40tmws.local" `
  -Method GET -Headers @{"Authorization"="Bearer $token"} -UseBasicParsing | Select-Object -ExpandProperty Content
```

### Frontend Tests (Browser)
1. Öffne `http://localhost:4200`
2. Login mit `admin@tmws.local` / `Admin@123456789`
3. Teste folgende Flows:
   - [ ] Profil anzeigen
   - [ ] Passwort ändern
   - [ ] Aufgabe erstellen
   - [ ] Aufgabe bearbeiten
   - [ ] Aufgabe verschieben (Drag & Drop)
   - [ ] Spalte hinzufügen
   - [ ] Spalte bearbeiten
   - [ ] Spalte löschen
   - [ ] Mitglied einladen

---

## 🔒 Sicherheitshinweise

### ✅ Implementiert
- JWT Token-basierte Authentifizierung
- BCrypt Passwort-Hashing
- CORS-Whitelisting
- CSRF Protection (disabled für REST)
- Token Expiration (24h)
- Secure Headers

### ⚠️ Production-Setup erforderlich
- [ ] JWT Secret in Umgebungsvariablen verschieben
- [ ] H2 In-Memory → PostgreSQL/MySQL
- [ ] Email-Service konfigurieren
- [ ] HTTPS/SSL-Zertifikate
- [ ] Rate Limiting
- [ ] Logging & Monitoring
- [ ] Error Tracking (Sentry)

---

## 💻 Technologie-Stack

### Backend
- **Java 26+**
- **Java Spring Boot 4+**
- **Java Spring Security**
- **Java Spring Data JPA**
- **JWT (io.jsonwebtoken)**
- **Gradle 9+**
- **H2 Database**

### Frontend
- **Angular 21+**
- **TypeScript 6+**
- **RxJS**
- **Node.js 26+**
- **pnpm 11+**

### DevOps
- **Gradle Wrapper**
- **Angular CLI**
- **Docker (optional)**

---

## 📁 Projektstruktur

```
Task-Management-Web-System/
├── backend/
│   ├── src/main/java/io/github/mexikoedi/tmws/
│   │   ├── config/           (CORS, Security, Data Init)
│   │   ├── controller/       (REST Endpoints)
│   │   ├── dto/              (Request/Response Objects)
│   │   ├── model/            (JPA Entities)
│   │   ├── repository/       (Java Spring Data JPA)
│   │   ├── service/          (Business Logic)
│   │   ├── security/         (JWT, Security Config)
│   │   ├── util/             (JWT Token Provider)
│   │   └── exception/        (Custom Exceptions)
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── h2/ (schema init files)
│   ├── build.gradle.kts
│   └── gradlew, gradlew.bat
├── frontend/
│   ├── src/app/
│   │   ├── component/
│   │   ├── core/             (Guards, Interceptors)
│   │   ├── model/            (TypeScript Interfaces)
│   │   ├── page/             (Page Components)
│   │   │   ├── dashboard/
│   │   │   ├── login/
│   │   │   ├── reset-password/
│   │   │   └── verify-email/
│   │   ├── service/          (API Services)
│   │   ├── shared/
│   │   ├── directive/
│   │   ├── app.routes.ts
│   │   ├── app.component.ts
│   │   └── main.ts
│   ├── package.json
│   ├── angular.json
│   └── tsconfig.json
├── CONFIGURATION.md
├── MAILPIT_SETUP.md
├── SETUP.md
├── README.md
├── README_FINAL.md
```

---

## 🐛 Troubleshooting

### Problem: "Cannot find module..."
```bash
cd frontend && pnpm install
```

### Problem: "Port 8080 already in use"
```powershell
# Find and kill Java process
Get-Process java | Stop-Process -Force
```

### Problem: "CORS Error"
- Stelle sicher, dass Frontend auf `localhost:4200` läuft
- Backend CORS Config prüfen: `backend/src/main/java/io/github/mexikoedi/tmws/config/CorsConfig.java`

### Problem: "401 Unauthorized"
- Stelle sicher, dass JWT Token im `Authorization: Bearer <token>` Header gesetzt ist
- Token-Expiration prüfen (24h gültig)

---

**Letzte Aktualisierung:** 2025-05-19
