# Task Management Web System (TMWS)

Ein Aufgabenmanagementsystem als Single Page Application mit Java Spring Boot Backend und Angular Frontend.

## Stack

- **Backend**: Java 26.0.1, Spring Boot 4.0.6, Spring Data JPA 4.0.5, Gradle 9.5.1
- **Frontend**: Angular 21.2.13, TypeScript 6.0.3
- **Datenbank**: H2 (In-Memory)
- **Architecture**: Client-Server REST API

## Projektstruktur

```
Task-Management-Web-System/
├── backend/                    # Spring Boot Backend
│   ├── src/
│   │   ├── main/java/io/github/mexikoedi/tmws/
│   │   │   ├── TmwsApplication.java
│   │   │   ├── config/         # Spring Configuration
│   │   │   ├── controller/     # REST Controller
│   │   │   ├── dto/            # Data Transfer Objects
│   │   │   ├── enum/           # Enums
│   │   │   ├── exception/      # Exception Classes
│   │   │   ├── model/          # JPA Entities
│   │   │   ├── repository/     # Spring Data Repository
│   │   │   ├── security/       # Security Configuration
│   │   │   ├── service/        # Business Logic
│   │   │   └── util/           # Utility Classes
│   │   ├── resources/
│   │   │   ├── application.properties
│   │   │   └── h2/
│   │   └── test/
│   ├── build.gradle.kts        # Gradle Build Script
│   ├── settings.gradle.kts
│   ├── gradlew                 # Gradle Wrapper (Linux/Mac)
│   ├── gradlew.bat             # Gradle Wrapper (Windows)
│   └── gradle/wrapper/
├── frontend/                   # Angular Frontend
│   ├── src/
│   │   ├── app/
│   │   │   ├── app.component.{ts,html,css}
│   │   │   ├── component/      # Reusable Components
│   │   │   ├── service/        # Services
│   │   │   ├── model/          # Data Models
│   │   │   ├── page/           # Page Components
│   │   │   ├── shared/         # Shared Modules
│   │   │   ├── directive/      # Custom Directives
│   │   │   └── core/           # Core Module
│   │   ├── index.html
│   │   ├── main.ts
│   │   ├── styles.css
│   │   └── assets/
│   ├── angular.json            # Angular CLI Configuration
│   ├── package.json
│   ├── package-lock.json
│   ├── tsconfig.json
│   ├── karma.conf.js           # Test Runner Configuration
│   └── .browserlistrc
├── .gitignore                  # Version Control Ignore
├── .editorconfig               # Editor Configuration
└── README.md                   # This file

```

## Setup und Installation

### Voraussetzungen

- **Java** 26.0.1 oder höher
- **Node.js** 26.2.0 oder höher
- **pnpm** 11.1.3 oder höher

### Backend Setup

1. **Gradle Wrapper initialisieren** (erste Zeit):
   ```bash
   cd backend
   gradle wrapper
   ```

2. **Dependencies installieren und Projekt bauen**:
   ```bash
   # Windows
   .\gradlew.bat build

   # Linux/Mac
   ./gradlew build
   ```

3. **Backend starten**:
   ```bash
   # Windows
   .\gradlew.bat bootRun

   # Linux/Mac
   ./gradlew bootRun
   ```

   Das Backend läuft dann auf `http://localhost:8080`

4. **H2 Database Console öffnen**:
   - Öffne `http://localhost:8080/h2-console`
   - URL: `jdbc:h2:mem:tmws`
   - Benutzername: `sa`
   - Passwort: (leer)

### Frontend Setup

1. **Abhängigkeiten installieren**:
   ```bash
   cd frontend
   pnpm install
   ```

2. **Entwicklungsserver starten**:
   ```bash
   pnpm start
   ```

   Das Frontend läuft dann auf `http://localhost:4200`

3. **Production Build**:
   ```bash
   pnpm run build
   ```

4. **Tests ausführen**:
   ```bash
   pnpm test
   ```

## Verfügbare Scripts

### Backend (Gradle)
- `gradlew build` - Projekt bauen
- `gradlew bootRun` - Anwendung starten
- `gradlew test` - Unit Tests ausführen
- `gradlew clean` - Build-Verzeichnis löschen

### Frontend (pnpm)
- `pnpm start` - ng serve --open
- `pnpm run build` - ng build
- `pnpm test` - ng test
- `pnpm run lint` - ng lint
- `pnpm run e2e` - ng e2e

## Technische Details

### Backend Features
- RESTful API
- Spring Data JPA für Datenschichtabstraktion
- H2 In-Memory Datenbank für Schnelltest/Entwicklung
- CORS-Unterstützung für Frontend-Zugriffe
- Exception Handling
- Config-Verwaltung

### Frontend Features
- Single Page Application (SPA)
- Responsive Design
- Component-based Architecture
- Services für API-Kommunikation
- TypeScript für Type Safety
- Unit Tests mit Jasmine/Karma

## Entwicklung

### Code Style Richtlinien

- Alle Dateien verwenden UTF-8 Encoding
- Java: 2 Spaces Indentation
- TypeScript/JavaScript: 2 Spaces Indentation
- HTML/CSS: 2 Spaces Indentation

Siehe `.editorconfig` für Editor-Konfiguration.

---

**Letzte Aktualisierung:** 2025-05-20
