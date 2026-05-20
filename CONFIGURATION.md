# TMWS - Konfiguration Übersicht

## 📋 Zusammenfassung der Konfiguration

Dieses Dokument fasst alle Konfigurationen zusammen, die für das Task Management Web System (TMWS) erstellt wurden.

---

## 🔧 Backend Konfiguration

### Build-Tool
- **Framework**: Gradle Kotlin DSL (9.5.1)
- **Java Spring Boot Version**: 4.0.6
- **Java Version**: 26.0.1

### Wichtige Dateien

| Datei | Zweck                                       |
|-------|---------------------------------------------|
| `build.gradle.kts` | Gradle Build-Konfiguration mit Dependencies |
| `settings.gradle.kts` | Gradle Settings & Projekt-Name              |
| `gradlew` / `gradlew.bat` | Gradle Wrapper Scripts (Windows & Unix)     |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle Version & Download-URL               |
| `src/main/resources/application.properties` | Java Spring Boot Konfiguration              |
| `src/main/java/io/github/mexikoedi/tmws/TmwsApplication.java` | Java Spring Boot Main Class                 |

### Dependencies

| Dependency | Zweck |
|-----------|-------|
| `spring-boot-starter-web` | REST API & Web-Support |
| `spring-boot-starter-data-jpa` | JPA & Hibernate ORM |
| `h2` | In-Memory Datenbank (Entwicklung) |
| `spring-boot-starter-test` | JUnit 5, Mockito, AssertJ |

### Konfigurierte Settings (`application.properties`)

```properties
server.port = 8080
spring.datasource.url = jdbc:h2:mem:tmws;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName = org.h2.Driver
spring.datasource.username = sa
spring.datasource.password = (empty)
spring.jpa.database-platform = org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto = update
spring.h2.console.enabled = true
spring.h2.console.path = /h2-console
spring.h2.console.settings.web-allow-others=true
logging.level.org.springframework=INFO
```

### REST Endpoints (Beispiele implementiert)

| Endpoint | Methode | Beschreibung |
|----------|---------|-------------|
| `/api/health` | GET | Health-Check Endpoint |
| `/h2-console` | GET/POST | H2 Database Console |

### Package-Struktur

```
io.github.mexikoedi.tmws/
├── config/           → CorsConfig.java (CORS-Konfiguration)
├── controller/       → HealthController.java (REST Endpoints)
├── dto/              → (Data Transfer Objects)
├── enum/             → (Enumerationen)
├── exception/        → (Exception Handling)
├── model/            → (JPA Entities)
├── repository/       → (Spring Data Repositories)
├── security/         → (Security Config)
├── service/          → (Business Logic)
└── util/             → (Utilities)
```

### CORS-Konfiguration

```
Erlaubte Origins:
  - http://localhost:4200 (Frontend)
  - http://localhost:3000 (Alternative)

Erlaubte Methods: GET, POST, PUT, DELETE, OPTIONS
Erlaubte Headers: *
Max Age: 3600 Sekunden
```

---

## 🎨 Frontend Konfiguration

### Build-Tool & Framework
- **Framework**: Angular 21.2.13
- **Language**: TypeScript 6.0.3
- **Package Manager**: pnpm 11.1.3
- **Node.js**: 26.2.0

### Wichtige Dateien

| Datei | Zweck                             |
|-------|-----------------------------------|
| `package.json` | pnpm Abhängigkeiten & Scripts     |
| `angular.json` | Angular CLI Konfiguration         |
| `tsconfig.json` | TypeScript Konfiguration (Global) |
| `tsconfig.app.json` | TypeScript Konfiguration (App)    |
| `tsconfig.spec.json` | TypeScript Konfiguration (Tests)  |
| `karma.conf.js` | Karma Test-Runner Konfiguration   |
| `.browserslistrc` | Browser-Kompatibilität            |

### Angular Struktur

```
src/
├── main.ts          → Anwendungs-Bootstrap
├── index.html       → HTML Entry Point
├── styles.css       → Global Styles
├── test.ts          → Test-Setup
├── app/
│   ├── app.component.ts/html/css        → Haupt-Komponente
│   ├── app.component.spec.ts            → Unit-Tests
│   ├── component/    → Wiederverwendbare Komponenten
│   ├── service/      → HTTP & Business Services
│   ├── model/        → TypeScript Data Models
│   ├── page/         → Seiten-Komponenten (Routing)
│   ├── shared/       → Shared Module/Components
│   ├── directive/    → Custom Directives
│   └── core/         → Core Module (Singletons)
└── assets/           → Statische Assets (Bilder, etc.)
```

### pnpm Scripts

| Script | Befehl | Ergebnis |
|--------|--------|----------|
| `start` | `ng serve --open` | Dev-Server (http://localhost:4200) |
| `build` | `ng build` | Production Build |
| `test` | `ng test` | Unit Tests (Karma) |
| `lint` | `ng lint` | Code Linting |
| `e2e` | `ng e2e` | E2E Tests |

### Konfigurierte Ports

- **Development Server**: http://localhost:4200
- **Backend API**: http://localhost:8080

### Dependencies

**Produktiv:**
- `@angular/core`, `@angular/common`, `@angular/forms`, etc. (v21.2.13)
- `rxjs` (v7.8.2) → Reactive Programming
- `zone.js` (v0.16.2) → Async Operations

**Entwicklung:**
- `@angular/cli` (v21.2.11) → CLI Tools
- `typescript` (v6.0.3) → Typsystem
- `karma` → Test-Runner
- `jasmine` → Unit-Test Framework

### TypeScript Einstellungen

```
Target: ES2022
Module: ES2022
Strict Mode: ON
Decorator Support: ON
Politicas:
  - String Type Checking
  - No Implicit Returns
  - No Fallthrough Cases
```

---

## 🗂️ Projekt-Struktur

### Haupt-Verzeichnisse

```
Task-Management-Web-System/
├── backend/              # Java Spring Boot Backend (Gradle)
│   ├── src/
│   │   ├── main/
│   │   └── test/
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── gradlew / gradlew.bat
│   └── gradle/wrapper/
│
├── frontend/             # Angular Frontend (pnpm/ng)
│   ├── src/
│   │   ├── app/
│   │   ├── index.html
│   │   └── assets/
│   ├── package.json
│   ├── pnpm-lock.yaml
│   ├── pnpm-workspace.yaml
│   ├── angular.json
│   └── tsconfig.json
│
├── README.md             # Projekt-Übersicht
├── SETUP.md              # Installationsanleitung
├── CONFIGURATION.md      # Diese Datei
├── MAILPIT_SETUP.md      # Mailpit Konfiguration
├── README_FINAL.md       # Projekt-Abschlussbericht
├── .gitignore            # Git-Konfiguration
├── .editorconfig         # Editor-Standards
├── setup.bat             # Windows Setup-Skript
└── setup.sh              # Unix Setup-Skript
```

### Ignorierte Dateien (`.gitignore`)

```
Gradle: build/, .gradle/, gradle-app.setting
Node: node_modules/, yarn.lock
IDE: .idea/, .vscode/, *.iml, *.swp, *~
Java: *.class, *.war, bin/, out/
Angular: /dist/, /out-tsc/, /.angular/, /coverage/
TypeScript: *.tsbuildinfo, pnpm-debug.log*
```

---

## ⚙️ Startup-Prozess

### Backend Start

```bash
cd backend
./gradlew bootRun    # Linux/Mac
gradlew.bat bootRun  # Windows
```

**Expected Output:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_|\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v4.0.6)

2025-05-03 12:00:00.000  INFO 12345 --- [main] i.g.m.t.TmwsApplication : Started TmwsApplication in 2.345 seconds (JVM running for 3.456)
```

**Verfügbare Endpunkte:**
- `http://localhost:8080/api/health` → {"status":"UP","message":"TMWS Backend is running","version":"0.0.1-SNAPSHOT"}
- `http://localhost:8080/h2-console` → H2 DB Web Console

### Frontend Start

```bash
cd frontend
pnpm install    # First time only
pnpm start      # or: ng serve --open
```

**Expected Output:**
```
✔ Compiled successfully.
Application bundle generated successfully. X.XX MB

Initial Chunk Files| Names         | Size
bundle.js          | main          | X.XX MB
styles.css         | styles        | XX.XX kB

Build at: 2025-05-03T12:00:00.000Z - Hash: abc123 - Time: XXXX ms
```

**Browser öffnet automatisch:** `http://localhost:4200`

---

## 🧪 Testing-Setup

### Backend (JUnit 5)

```bash
cd backend
./gradlew test    # Alle Tests ausführen
./gradlew test --continue   # Alle Tests (auch bei Fehler)
```

**Test-Datei:** `src/test/java/io/github/mexikoedi/tmws/TmwsApplicationTests.java`

### Frontend (Karma + Jasmine)

```bash
cd frontend
pnpm test    # Karma Test-Runner öffnet Chrome Browser
```

**Test-Datei:** `src/app/app.component.spec.ts`

---

## 🔐 Sicherheit & Datenschutz

### H2 Console Zugriff

**URL:** `http://localhost:8080/h2-console`

```
JDBC URL: jdbc:h2:mem:tmws
Username: sa
Password: (leave empty)
```

⚠️ **WARNUNG:** H2-Console ist nur in Entwicklung aktiviert. Im Production muss `spring.h2.console.enabled=false` sein.

### CORS-Einstellungen

Nur folgende Ursprünge dürfen auf das Backend zugreifen:
- `http://localhost:4200` (Frontend Dev-Server)
- `http://localhost:3000` (Alternative)

### API-Authentifizierung

Spring Security + JWT Tokens

---

## 📦 Abhängigkeiten Versionen

### Backend

| Dependency       | Version | Zweck |
|------------------|---------|-------|
| Java Spring Boot | 4.0.6   | Framework |
| Spring Data JPA  | 4.0.5   | ORM |
| H2 Database      | 2.4.240 | Dev Database |
| JUnit Platform   | 5       | Test Framework |

### Frontend

| Dependency | Version | Zweck |
|-----------|---------|-------|
| Angular | 21.2.13 | Framework |
| TypeScript | 6.0.3   | Language |
| RxJS | 7.8.2   | Reactive |
| Zone.js | 0.16.2  | Async |

---

## 🚀 Production-Deployment

### Backend

```bash
cd backend

# Production Build
./gradlew build -Dspring.profiles.active=production

# JAR ausführen
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar
```

### Frontend

```bash
cd frontend

# Production Build
pnpm run build -- --configuration production

# Resultat in dist/frontend/
```

---

## 🐛 Troubleshooting-Checkliste

- [ ] Java 26.0.1 installiert? `java -version`
- [ ] Node.js 26.2.0 installiert? `node --version`
- [ ] Gradle Wrapper lädt herunter?
- [ ] pnpm install zonder errors?
- [ ] Backend läuft auf :8080?
- [ ] Frontend läuft auf :4200?
- [ ] CORS funktioniert (Backend headers)?
- [ ] H2-Console verfügbar?

---

## 📚 Weitere Ressourcen

- [Java Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Angular Docs](https://angular.io/docs)
- [Gradle User Guide](https://docs.gradle.org/current/userguide/)
- [H2 Database](https://www.h2database.com/)

---

**Letzte Aktualisierung:** 2025-05-20
