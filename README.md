# Task Management Web System (TMWS)

Ein modernes Aufgabenmanagementsystem (Kanban-Board) als Single Page Application mit Java Spring Boot Backend und Angular Frontend. Das System bietet vollständige Authentifizierung, Board-Management und Real-time-Updates via WebSocket.

## 📋 Inhaltsverzeichnis

- [Features](#-features)
- [Technologie-Stack](#-technologie-stack)
- [Voraussetzungen](#-voraussetzungen)
- [Schnelleinstieg](#-schnelleinstieg)
- [Konfiguration](#-konfiguration)
- [Architektur](#-architektur)
- [Häufige Befehle](#-häufige-befehle)
- [Datenbankzugriff](#-datenbankzugriff)
- [E-Mail-Setup (Mailpit)](#-e-mail-setup-mailpit)
- [Tests](#-tests)
- [Fehlerbehebung](#-fehlerbehebung)

---

## 🎯 Features

### ✅ Authentifizierung
- Login mit JWT Token-basierter Authentifizierung
- Benutzerregistrierung mit E-Mail-Verifikation
- Passwort-Reset via E-Mail
- Sichere Passwort-Speicherung (BCrypt)
- Single-Session-Pro-User Erzwingung
- Account-Verwaltung und Deaktivierung

### ✅ Kanban-Board
- Mehrere Bretter im klassischen Kanban-Format
- Standard 3-Spalten-Layout (TODO, Doing, Done) - erweiterbar
- Aufgaben erstellen, bearbeiten, löschen
- Drag & Drop zwischen Spalten (mit Position-Tracking)
- Neue Spalten hinzufügen, bearbeiten, löschen
- Labels und Anhänge pro Aufgabe
- Aufgabe-Zuweisungen (Mehrfach-Zuweisungen)
- Deadline-Tracking
- Real-time-Updates via WebSocket

### ✅ Board-Management
- Bretter erstellen, bearbeiten, löschen
- Benutzerfreigabe (Members hinzufügen/entfernen)
- Benutzerdefinierte Hintergrundfarben
- Owner- und Member-Rollen

### ✅ Benutzerprofile
- Profilansicht mit Avatar (generierte Initialen)
- Namen ändern
- E-Mail-Verwaltung
- Passwort ändern
- Account-Verwaltung und Deaktivierung

### ✅ Suche & Filterung
- Live-Suchleiste für Aufgaben
- Intelligente Vorschläge basierend auf Titeln

---

## 🚀 Technologie-Stack

### Backend
| Komponente | Version |
|-----------|---------|
| **Java** | 26.0.1 |
| **Spring Boot** | 4.0.6 |
| **Spring Data JPA** | 4.0.5 |
| **Spring Security** | 4.0.6 |
| **Spring WebSocket** | 4.0.6 |
| **Gradle** | 9.5.1 |
| **H2 Database** | 2.4.240 (In-Memory) |
| **JWT (JJWT)** | 0.13.0 |
| **Lombok** | 1.18.46 |
| **JUnit 5** | Latest (via Spring Boot) |

### Frontend
| Komponente | Version |
|-----------|---------|
| **Angular** | 21.2.13 |
| **Angular CLI** | 21.2.11 |
| **TypeScript** | 6.0.3 |
| **RxJS** | 7.8.2 |
| **Zone.js** | 0.16.2 |
| **pnpm** | 11.1.3 |
| **Node.js** | 26.2.0+ |

### Zusätzliche Tools
- **Bootstrap Icons** (ng-icons)
- **ng-select** für Dropdown-Komponenten
- **Datepicker** (ngxsmk-datepicker)
- **SweetAlert2** für Benachrichtigungen
- **Stomp.js** für WebSocket-Kommunikation
- **ESLint** | Code-Linting
- **Prettier** | Code-Formatierung
- **Stylelint** | CSS-Linting
- **Karma + Jasmine** | Unit-Testing
- **Mailpit** | Fake SMTP für E-Mails (optional)

---

## 📦 Voraussetzungen

Stelle sicher, dass folgende Software installiert ist:

```bash
# Erforderliche Versionen
Java 26.0.1 oder höher      (https://adoptium.net/)
Node.js 26.2.0 oder höher   (https://nodejs.org/)
Git 2.54.0 oder höher       (https://git-scm.com/)
pnpm 11.1.3 oder höher      (npm install -g pnpm)
```

### Installation überprüfen

```bash
# Überprüfe die installierten Versionen
java -version
node --version
pnpm --version
git --version
```

---

## 🎬 Schnelleinstieg

### 1. Repository klonen

```bash
git clone https://github.com/mexikoedi/Task-Management-Web-System.git
cd Task-Management-Web-System
```

### 2. Backend starten

```bash
cd backend

# Option A: Mit Gradle Wrapper (empfohlen - keine Installation erforderlich)
./gradlew bootRun        # macOS/Linux
gradlew.bat bootRun      # Windows

# Option B: Mit lokalem Gradle (falls installiert)
gradle bootRun

# Das Backend läuft auf: http://localhost:8080
```

### 3. Frontend starten (separates Terminal)

```bash
cd frontend

# Dependencies installieren
pnpm install

# Entwicklungsserver starten (öffnet Browser automatisch)
pnpm start

# Das Frontend läuft auf: http://localhost:4200
```

### 4. Anmeldung testen

**Demo-Benutzer (oder nach Registrierung anmelden):**
- E-Mail: `demo@tmws.local`
- Passwort: `Demo@1234567`

---

## ⚙️ Konfiguration

### Backend-Konfiguration (`backend/src/main/resources/application.properties`)

```properties
# Server
server.port=8080

# H2 Database (In-Memory)
spring.datasource.url=jdbc:h2:mem:tmws;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.h2.console.settings.web-allow-others=true

# JWT
jwt.secret=mySecretKeyForJWTTokenGenerationAndValidationMustBeAtLeast256bitsForHS256Algorithm
jwt.expiration=86400000

# Mail (Mailpit)
spring.mail.host=localhost
spring.mail.port=1025
spring.mail.username=
spring.mail.password=
spring.mail.properties.mail.smtp.auth=false
spring.mail.properties.mail.smtp.starttls.enable=false

# Logging
logging.level.root=INFO
logging.level.io.github.mexikoedi=DEBUG
```

### Frontend-Konfiguration (`frontend/angular.json`)

```json
{
  "projects": {
    "tmws-frontend": {
      "targets": {
        "serve": {
          "options": {
            "port": 4200,
            "host": "localhost",
            "open": true
          }
        }
      }
    }
  }
}
```

### CORS-Konfiguration (Backend)

Erlaubte Domains sind in `backend/src/main/java/.../config/CorsConfig.java` konfiguriert:

```
// Erlaubte Origins
- http://localhost:4200 (Entwicklung)
- http://localhost:3000  (Alternative)

// Erlaubte HTTP-Methoden: GET, POST, PUT, DELETE, OPTIONS
// Erlaubte Headers: *
// Max Age: 3600 Sekunden
```

---

## 🏗️ Architektur

### Backend - MVC Pattern

```
io.github.mexikoedi.tmws/
├── config/              # Spring Configuration, CORS, WebSocket, Security
├── controller/          # REST Endpoints (@RestController)
├── dto/                 # Data Transfer Objects (Request/Response)
├── enum/               # Enumerationen (TaskStatus, UserRole, etc.)
├── exception/          # Custom Exception Handling
├── model/              # JPA Entities (@Entity)
├── repository/         # Spring Data Repositories (Data Access)
├── security/           # Spring Security Configuration
├── service/            # Business Logic Services
├── util/               # Utility Classes (JWT, Validation, etc.)
└── TmwsApplication.java # Main Application Class
```

### Frontend - Angular-Struktur

```
src/app/
├── app.component.*      # Root Component
├── app.routes.ts        # Routing Configuration
├── component/           # Reusable Components
├── core/               # Core Services (Auth, Guards, Interceptors)
│   ├── guards/         # Auth Guards, Route Protectors
│   └── interceptors/   # HTTP Interceptors (Token-Handling)
├── directive/          # Custom Directives
├── model/              # TypeScript Interfaces & Models
├── page/               # Page Components (Dashboard, Login, etc.)
├── service/            # Application Services
│   ├── auth.service.ts
│   ├── board.service.ts
│   ├── heartbeat.service.ts
│   └── websocket.service.ts
└── shared/             # Shared Modules & Components
```

### Datenbank-Schema

Die H2 In-Memory Datenbank wird automatisch beim Start initialisiert mit:

**Haupttabellen:**
- `users` - Benutzer mit Authentifizierung
- `boards` - Kanban Bretter
- `board_columns` - Spalten (TODO, Doing, Done)
- `tasks` - Aufgaben mit Zuweisungen
- `task_assignees` - Many-to-Many Zuweisungen
- `verification_tokens` - E-Mail-Verifikation
- `password_reset_tokens` - Passwort-Zurücksetzen

---

## 🔧 Häufige Befehle

### Backend (Gradle)

```bash
cd backend

# Build & Test
./gradlew build              # Clean build + tests
./gradlew assemble           # Nur assemblieren (ohne Tests)
./gradlew test               # Nur Tests ausführen
./gradlew clean              # Aufräumen

# Execution
./gradlew bootRun                              # Anwendung starten
./gradlew bootRun --args='--server.port=8081'  # Mit anderem Port

# Information
./gradlew tasks                     # Alle verfügbaren Tasks anzeigen
./gradlew dependencies              # Dependencies auflisten
./gradlew check --warning-mode all  # Mit allen Warnungen
```

### Frontend (pnpm)

```bash
cd frontend

# Development
pnpm start                               # Dev-Server starten (http://localhost:4200)
pnpm build                               # Production Build
pnpm build --configuration production    # Mit Optimierungen

# Testing
pnpm test                    # Unit Tests (Karma/Jasmine)
pnpm test -- --code-coverage # Mit Coverage-Report
pnpm e2e                     # E2E Tests
pnpm e2e:ui                  # E2E Tests mit UI-Feedback

# Code Quality
pnpm lint                    # ESLint ausführen
pnpm lint --fix              # ESLint mit Auto-Fix
pnpm stylelint .             # Stylelint ausführen
pnpm stylelint . --fix       # Stylelint ausführen mit Auto-Fix

# Formatting
pnpm prettier . --w          # Mit Prettier formatieren
```

---

## 💾 Datenbankzugriff

### H2 Console

Nach dem Backend-Start kannst du die Datenbank über die H2 Web Console ansehen:

**URL:** http://localhost:8080/h2-console

**Verbindung:**
- JDBC URL: `jdbc:h2:mem:tmws`
- Benutzer: `sa`
- Passwort: (leer lassen)

### SQL Queries Beispiele

```
-- Alle Benutzer anzeigen
SELECT * FROM users;

-- Alle Bretter eines Benutzers
SELECT * FROM boards WHERE owner_id = 1;

-- Aufgaben in einer Spalte
SELECT * FROM tasks WHERE column_id = 1 ORDER BY position_index;
```

---

## 📧 E-Mail-Setup (Mailpit)

Mailpit ist ein Fake SMTP-Server für lokale Entwicklung. E-Mails werden nicht wirklich versendet, sondern lokal gehostet.

### Installation Option 1: Docker (empfohlen)

```bash
# Mailpit mit Docker starten
docker run -d -p 1025:1025 -p 8025:8025 --name mailpit axllent/mailpit

# Portierung:
# - SMTP: 1025 (Backend sendet Mails hier)
# - Web-UI: 8025 (E-Mails anschauen)
```

### Installation Option 2: Direkter Download

1. Lade Mailpit herunter: https://github.com/axllent/mailpit/releases
2. Entpacke die Datei
3. Starte Mailpit:
   ```bash
   ./mailpit              # macOS/Linux
   mailpit.exe            # Windows
   ```

### Verwendung

Nach Registrierung oder Passwort-Reset besuche:

**http://localhost:8025** - Alle versandten E-Mails werden hier angezeigt

---

## 🧪 Tests

### Backend Tests

```bash
cd backend

# Alle Tests ausführen
./gradlew test

# Spezifischen Test ausführen
./gradlew test --tests "io.github.mexikoedi.tmws.controller.*"

# Mit Coverage
./gradlew test --jacoco
```

### Frontend Tests

```bash
cd frontend

# Unit Tests mit Karma/Jasmine
pnpm test

# Mit Code Coverage
pnpm test -- --code-coverage

# Watch Mode (kontinuierlich)
pnpm test -- --watch
```

### E2E Tests

```bash
cd frontend

# E2E Tests ausführen
pnpm e2e
```

---

## 🎨 Code-Formatierung & Linting

### Frontend

```bash
cd frontend

# ESLint
pnpm lint              # Prüfen
pnpm lint --fix        # Auto-Fix

# Prettier
pnpm format            # Formatieren

# Stylelint
pnpm lint:styles       # CSS prüfen
```

---

## 📱 API-Endpoints

### Authentifizierung

```
POST   /api/auth/register       # Registrierung
POST   /api/auth/login          # Anmeldung
POST   /api/auth/verify-email   # E-Mail verifizieren
POST   /api/auth/reset-password # Passwort zurücksetzen
```

### Benutzer

```
GET    /api/users/me            # Aktuellen Benutzer abrufen
PUT    /api/users/profile       # Profil aktualisieren
PUT    /api/users/password      # Passwort ändern
DELETE /api/users/account       # Account deaktivieren
```

### Bretter

```
GET    /api/boards              # Alle Bretter abrufen
POST   /api/boards              # Neues Brett erstellen
GET    /api/boards/{id}         # Brett-Details abrufen
PUT    /api/boards/{id}         # Brett aktualisieren
DELETE /api/boards/{id}         # Brett löschen
```

### Aufgaben

```
GET    /api/boards/{id}/tasks   # Aufgaben eines Bretts
POST   /api/tasks               # Neue Aufgabe erstellen
PUT    /api/tasks/{id}          # Aufgabe aktualisieren
DELETE /api/tasks/{id}          # Aufgabe löschen
```

### Sonstiges

```
GET    /api/health              # Health Check
```

---

## 🐛 Fehlerbehebung

### Backend-Probleme

**Port 8080 bereits in Verwendung:**
```bash
# Anderen Port verwenden
./gradlew bootRun --args='--server.port=8081'
```

**H2 Console nicht erreichbar:**
- Überprüfe `application.properties`: `spring.h2.console.enabled=true`
- URL: http://localhost:8080/h2-console

**JWT Token-Fehler:**
- Überprüfe `jwt.secret` in `application.properties`
- Muss mindestens 32 Zeichen lang sein für HS256

### Frontend-Probleme

**Port 4200 bereits in Verwendung:**
```bash
pnpm start -- --port 4300
```

**Abhängigkeiten nicht installiert:**
```bash
rm -rf node_modules pnpm-lock.yaml
pnpm install
```

### Verbindungsprobleme

**Frontend kann Backend nicht erreichen:**
- Überprüfe CORS-Konfiguration
- Backend läuft auf http://localhost:8080?
- Frontend läuft auf http://localhost:4200?
- Überprüfe Browser-Console auf Errors

---

## 📚 Weitere Ressourcen

- **Spring Boot**: https://spring.io/projects/spring-boot
- **Angular**: https://angular.io/docs
- **JWT.io**: https://jwt.io/
- **Mailpit**: https://mailpit.axllent.org/

---

**Letzte Aktualisierung:** 21.05.2026
