# TMWS - Setup Anleitung

## Schnelleinstieg

### Schritt 1: Voraussetzungen installieren

Stelle sicher, dass du folgende Software installiert hast:

- **Java 26.0.1** oder höher ([Download](https://adoptium.net/))
- **Node.js 26.2.0** oder höher ([Download](https://nodejs.org/))
- **Git 2.54.0** ([Download](https://git-scm.com/))

Überprüfe die Installationen:

```bash
java -version
node --version
pnpm --version
git --version
```

### Schritt 2: Projekt Setup

#### Automatisches Setup (Windows)

```bash
cd Task-Management-Web-System
setup.bat
```

#### Automatisches Setup (macOS/Linux)

```bash
cd Task-Management-Web-System
chmod +x setup.sh
./setup.sh
```

### Schritt 3: Backend Setup und Start

```bash
cd backend

# Projekt bauen
./gradlew build     # Linux/Mac
gradlew.bat build   # Windows

# Oder direkt starten (Build automatisch)
./gradlew bootRun    # Linux/Mac
gradlew.bat bootRun  # Windows
```

Das Backend läuft dann auf: **http://localhost:8080**

**Test-Endpoints:**
- Health Check: `http://localhost:8080/api/health`
- H2 Console: `http://localhost:8080/h2-console`
  - URL: `jdbc:h2:mem:tmws`
  - Username: `sa`
  - Password: (leave empty)

### Schritt 4: Frontend Setup und Start

```bash
cd frontend

# Dependencies installieren
pnpm install

# Entwicklungsserver starten (öffnet Browser automatisch)
pnpm start
```

Das Frontend läuft dann auf: **http://localhost:4200**

## Detaillierte Konfiguration

### Backend Konfiguration

#### `build.gradle.kts` - Build-Konfiguration
- Spring Boot 4.0.6 Starter Web
- Spring Data JPA für Datenbankzugriff
- H2 Database (in-memory)
- JUnit 5 für Tests

#### `application.properties` - Runtime-Konfiguration
```properties
server.port = 8080                                                                 # Server-Port
spring.datasource.url = jdbc:h2:mem:tmws;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE  # H2 In-Memory DB
spring.datasource.driverClassName = org.h2.Driver                                  # H2 JDBC Driver
spring.datasource.username = sa                                                    # Standard-Username für H2
spring.datasource.password = (empty)                                               # Standard-Passwort für H2
spring.jpa.database-platform = org.hibernate.dialect.H2Dialect                     # Hibernate Dialect für H2
spring.jpa.hibernate.ddl-auto = update                                             # Auto-DDL
spring.h2.console.enabled = true                                                   # H2-Console aktivieren
spring.h2.console.path = /h2-console                                               # Pfad für H2-Console
spring.h2.console.settings.web-allow-others=true                                   # Zugriff von anderen Hosts erlauben
logging.level.org.springframework=INFO                                             # Logging-Level für Spring Framework
```

#### Gradle Wrapper
- Automatisches Download von Gradle 9.5.1
- Cross-platform Unterstützung (Windows, Linux, macOS)

### Frontend Konfiguration

#### `package.json` - Abhängigkeiten
- Angular Core 21.2.13 & CLI 21.2.11
- TypeScript 6.0.3
- RxJS 7.8.2
- Zone.js 0.16.2 für Async Operations

#### `angular.json` - Angular CLI Konfiguration
- Build-Targets (Production/Development)
- Serve-Konfiguration auf Port 4200
- Test-Setup mit Karma/Jasmine

#### `tsconfig.json` - TypeScript Konfiguration
- Target: ES2022
- Strict Mode aktiviert
- Dekoratoren aktiviert

## Häufige Befehle

### Backend (Gradle)

```bash
cd backend

# Build
./gradlew build          # Online Build mit Tests
./gradlew assemble       # Nur assemblieren
./gradlew clean          # Cleanup

# Execution
./gradlew bootRun        # Anwendung starten
./gradlew test           # Unit Tests ausführen

# Infos
./gradlew tasks          # Alle Tasks anzeigen
./gradlew dependencies   # Dependencies anzeigen
```

### Frontend (pnpm)

```bash
cd frontend

# Development
pnpm start                # Dev-Server starten (http://localhost:4200)
pnpm run build            # Production Build
pnpm test                 # Unit Tests (Chrome, interactive)

# Production
pnpm run build -- --configuration production
pnpm run lint             # Code Linting
```

## Architektur

### Backend MVC Struktur

```
io.github.mexikoedi.tmws/
├── controller/       # REST Endpoints (@RestController)
├── service/          # Business Logic
├── model/            # JPA Entities (@Entity)
├── repository/       # Data Access (@Repository)
├── dto/              # Data Transfer Objects
├── config/           # Spring Configuration
├── security/         # Security Configuration
├── exception/        # Custom Exceptions
├── enum/             # Enumerationen
└── util/             # Utility Classes
```

### Frontend Angular Struktur

```
src/app/
├── app.component.*         # Root Component
├── component/              # Reusable Components
├── service/                # HTTP Services
├── model/                  # TypeScript Data Models
├── page/                   # Page/Route Components
├── shared/                 # Shared Module
├── directive/              # Custom Directives
└── core/                   # Core Module (Singletons)
```

## API Kommunikation

Der Frontend kommuniziert mit dem Backend über REST API auf `/api/*` Endpoints.

**CORS ist konfiguriert für:**
- `http://localhost:4200` (Frontend)
- `http://localhost:3000` (Alternative)

**HTTP Methods:** GET, POST, PUT, DELETE

## Problembehebung

### Gradle Wrapper funktioniert nicht
- Stelle sicher, dass Java 26.0.1 installiert ist
- Prüfe JAVA_HOME Environment Variable
- Versuche: `gradle wrapper --gradle-version 9.5.1`

### pnpm install fehlgeschlagen
- Lösche `node_modules` und `package-lock.json`
- Versuche: `pnpm cache clean --force && pnpm install`
- Prüfe Netzwerk-Verbindung

### Port bereits in Verwendung
```bash
# Backend (8080) freigeben
netstat -ano | findstr :8080           # Windows
lsof -i :8080                          # macOS/Linux

# Frontend (4200) freigeben
netstat -ano | findstr :4200           # Windows
lsof -i :4200                          # macOS/Linux
```

### Build fehlgeschlagen
- Cleanup: `./gradlew clean build`
- Tests überspringen: `./gradlew build -x test`
- Verbose Logging: `./gradlew build --debug`

## IDE Setup

### IntelliJ IDEA
1. File → Open → wähle Projektroot
2. IDE erkennt Gradle automatisch
3. Konfiguriere SDK: Java 26.0.1

### VS Code
1. Installiere: Spring Boot Extension Pack
2. Installiere: Angular Language Service
3. F5 zum Debuggen

## Nächste Schritte

1. Entities in `model/` anlegen
2. Repository Interfaces in `repository/` erstellen
3. Services in `service/` implementieren
4. REST Controller in `controller/` schreiben
5. Angular Components in `component/` entwickeln
6. Services/Models in `service/` und `model/` definieren

---

**Letzte Aktualisierung:** 2025-05-20
