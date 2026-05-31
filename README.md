# Task Management Web System
Ein modernes Aufgabenmanagementsystem (Kanban-Projektboard) als Single Page Application mit Java Spring Boot Backend und Angular Frontend. <br>
Das System bietet vollständige Authentifizierung, Projektboard-Management und Real-time-Updates via WebSocket. <br>
Es ist im Rahmen meiner Bachelor-Thesis unter dem Titel "Unterstützung der Softwareentwicklung durch KI-basierte Code-Generierung: Eine empirische Untersuchung am Beispiel von GitHub Copilot" entstanden.

---

## Features

### Authentifizierung
- Benutzerregistrierung mit E-Mail-Verifikation
- Login mit JWT-basierter Authentifizierung
- Passwort-Zurücksetzung via E-Mail
- Sichere Passwort-Speicherung (BCrypt)

### Kanban-Projektboard
- Mehrere Statuskategorien im klassischen Kanban-Format
- Standard 3-Statuskategorien-Layout (Demnächst, In Bearbeitung, Fertig) - erweiterbar
- Aufgaben erstellen, umbenennen, bearbeiten, löschen
- Titel, Beschreibung, Deadline (Datepicker), Labels, Anhänge und Zuweisung an Benutzer pro Aufgabe
- Aufgaben in andere Statuskategorien durch Drag & Drop verschiebbar
- Neue Statuskategorien erstellen, umbenennen, bearbeiten, löschen
- Statuskategorien können auch durch Drag & Drop neu angeordnet werden
- Real-time-Updates via WebSocket

### Projektboard-Management
- Titel vom Projektboard kann geändert werden
- Benutzer können zu Projektboard eingeladen werden
- Man kann zwischen den Projektboards wechseln
- Benutzerdefiniertes Hintergrundbild möglich

### Benutzerprofile
- Account-Verwaltung (Name, E-Mail, Profilbild, Passwort, Deaktivierung)
- Profilbilder oder generierte Initialen sichtbar im Projektboard

### Suche und Filterung
- Live-Suchleiste für Aufgaben
- Vorschläge basierend auf Titeln

---

## Beispiele
So sieht das Login-Fenster aus: <br>
<img width="763" height="720" alt="tmws_login_fenster" src="https://github.com/user-attachments/assets/c252d067-b072-4b91-9772-7f1c707ec5bf" />

Und hier kann man den Hauptbereich mit dem Projektboard, nach der erfolgreichen Anmeldung, sehen: <br>
<img width="1904" height="910" alt="tmws_hauptansicht_fenster" src="https://github.com/user-attachments/assets/e2993e98-3a88-49c7-9592-a77f0a9d24fc" />

---

## Technologie-Stack

### Backend
| Komponente                       | Version |
|----------------------------------|---------|
| **Gradle**                       | 9.5.1   |
| **Java**                         | 26.0.1  |
| **Spring Boot**                  | 4.0.6   |
| **Dependency Management Plugin** | 1.1.7   |
| **FreeFair Lombok**              | 9.5.0   |
| **Spring Starter Web MVC**       | 4.0.6   |
| **Spring Starter WebSocket**     | 4.0.6   |
| **Spring Starter Data JPA**      | 4.0.6   |
| **Spring H2console**             | 4.0.6   |
| **H2 Database Engine**           | 2.4.240 |
| **Spring Starter Validation**    | 4.0.6   |
| **Spring Starter Mail**          | 4.0.6   |
| **Spring Starter Security**      | 4.0.6   |
| **JJWT API**                     | 0.13.0  |
| **JJWT Impl**                    | 0.13.0  |
| **JJWT Jackson**                 | 0.13.0  |
| **JJWT Jackson**                 | 0.13.0  |
| **Spring Boot Starter Test**     | 4.0.6   |

### Frontend
| Komponente (Normale)            | Version    |
|---------------------------------|------------|
| **Node.js**                     | 26.2.0     |
| **pnpm**                        | 11.5.0     |
| **@angular/common**             | 21.2.15    |
| **@angular/compiler**           | 21.2.15    |
| **@angular/core**               | 21.2.15    |
| **@angular/forms**              | 21.2.15    |
| **@angular/platform-browser**   | 21.2.15    |
| **@angular/router**             | 21.2.15    |
| **@ng-icons/core**              | 33.2.3     |
| **@ng-icons/bootstrap-icons**   | 33.2.3     |
| **@ng-select/ng-select**        | 21.8.2     |
| **ngxsmk-datepicker**           | 2.2.15     |
| **@stomp/stompjs**              | 7.3.0      |
| **rxjs**                        | 7.8.2      |
| **sweetalert2**                 | 11.26.25   |
| **tslib**                       | 2.8.1      |

| Komponente (Dev)              | Version    |
|-------------------------------|------------|
| **@angular/build**            | 21.2.13    |
| **@angular/cli**              | 21.2.13    |
| **@angular/compiler-cli**     | 21.2.15    |
| **angular-eslint**            | 21.4.0     |
| **@angular-eslint/builder**   | 21.4.0     |
| **eslint**                    | 10.4.1     |
| **@eslint/js**                | 10.0.1     |
| **eslint-config-prettier**    | 10.1.8     |
| **eslint-plugin-prettier**    | 5.5.6      |
| **prettier**                  | 3.8.3      |
| **stylelint**                 | 17.12.0    |
| **stylelint-config-standard** | 40.0.0     |
| **typescript**                | 6.0.3      |
| **typescript-eslint**         | 8.60.0     |
| **vitest**                    | 4.1.7      |
| **@playwright/test**          | 1.60.0     |
| **happy-dom**                 | 20.9.0     |

### Zusätzliche Tools
- **IntelliJ IDEA** - IDE für Entwicklung
- **GitHub Copilot** - KI-basierte Code-Generierung
- **Git** - Versionskontrolle
- **Mailpit** - Fake-SMTP-Server für E-Mails

---

## Voraussetzungen
Stelle sicher, dass folgende Software installiert ist:

```
IntelliJ IDEA (oder andere IDE für Java und Angular)
Mailpit (für E-Mail-Testing)
Java 26.0.1 oder höher
Node.js 26.2.0 oder höher
pnpm 11.5.0 oder höher
```

### Installation überprüfen
```
java -version
node --version
pnpm --version
```

---

## Schnelleinstieg

### 1. Repository klonen
```
git clone https://github.com/mexikoedi/Task-Management-Web-System.git
cd Task-Management-Web-System
```

### 2. Konfiguration anpassen
Siehe [Konfiguration](#konfiguration) weiter unten für wichtige Details zur Backend- und Frontend-Konfiguration.

### 3. Backend starten
```
cd backend

# Mit Gradle Wrapper starten
./gradlew bootRun

# Das Backend läuft auf: http://localhost:8080
```

### 4. Frontend starten (separates Terminal)
```
cd frontend

# Dependencies installieren
pnpm install

# Entwicklungsserver starten (öffnet Browser automatisch)
pnpm start

# Das Frontend läuft auf: http://localhost:4200
```

## 5. Fake-SMTP-Server (Mailpit) starten
```
# In einem separaten (externen) Terminal starten
macOS/Linux: mailpit
Windows: mailpit.exe

# Der SMTP-Server läuft auf Port http://localhost:1025 (Backend sendet Mails hier) und die Web-UI für E-Mails ist unter http://localhost:8025 erreichbar.
```

### 6. Anmeldung/E-Mail-Versand testen
Nach Registrierung und E-Mail-Verifikation mit Folgendem anmelden:
- E-Mail
- Passwort

---

## Konfiguration

### Backend-Konfiguration
Im Backend Ornder unter den jeweiligen Konfigurationsdateien einsehbar. <br>
Zum Beispiel `backend/src/main/resources/application.properties`.

#### Wichtig:
Unter `backend/src/main/resources` muss vor dem Start eine `application.local.properties` erstellt werden. <br>
Diese enthält sensible Informationen wie Benutzernamen, Passwörter und den JWT-Secret-Key:
```
## Benutzernamen und Passwörter für die Datenbank, Spring Security und E-Mail-Konfiguration
DATASOURCE_USERNAME=YOUR_DB_USERNAME
DATASOURCE_PASSWORD=YOUR_DB_PASSWORD
JPA_USERNAME=YOUR_DB_USERNAME
JPA_PASSWORD=YOUR_DB_PASSWORD
SECURITY_USERNAME=YOUR_SECURITY_USERNAME
SECURITY_PASSWORD=YOUR_SECURITY_PASSWORD
MAIL_USERNAME=YOUR_MAIL_USERNAME
MAIL_PASSWORD=YOUR_MAIL_PASSWORD

## JWT Secret und Expiration
JWT_SECRET=YOUR_JWT_SECRET (mindestens 256 bit (32 byte) lang für HS256 Algorithmus)
JWT_EXPIRATION=YOUR_JWT_EXPIRATION (in Millisekunden)
```

### Frontend-Konfiguration
Im Frontend Ordner unter den jeweiligen Konfigurationsdateien einsehbar. <br>
Zum Beispiel `frontend/angular.json`.

---

## Architektur
Das Backend läuft auf Java Spring Boot mit einem klassischen MVC-Pattern, während das Frontend mit Angular als Single
Page Application (SPA) aufgebaut ist. <br>
Die Kommunikation zwischen beiden erfolgt über REST und WebSockets für Echtzeit-Updates. <br>
Außerdem ist die Ornder-Struktur klar unterteilt, um eine saubere, moderne Trennung der Verantwortlichkeiten zu gewährleisten.

### Datenbank-Schema
Die H2 In-Memory Datenbank wird automatisch beim Start initialisiert mit:

**Haupttabellen:**
- `users` - Benutzer mit Authentifizierung
- `boards` - Kanban Projektboards
- `board_columns` - Statuskategorien
- `board_members` - Many-to-Many Benutzer-Projektboard-Zuordnung
- `tasks` - Aufgaben mit Zuweisungen
- `task_assignees` - Many-to-Many Aufgaben-Benutzer-Zuordnung
- `verification_tokens` - E-Mail-Verifikation
- `password_reset_tokens` - Passwort-Zurücksetzen

---

## Häufige Befehle

### Backend (Gradle)
```
cd backend

# Build & Test
./gradlew build                                 # Clean build + tests
./gradlew assemble                              # Nur assemblieren (ohne Tests)
./gradlew test                                  # Nur Tests ausführen
./gradlew clean                                 # Aufräumen

# Execution
./gradlew bootRun                               # Anwendung starten
./gradlew bootRun --args='--server.port=8081'   # Mit anderem Port

# Information
./gradlew tasks                                 # Alle verfügbaren Tasks anzeigen
./gradlew dependencies                          # Dependencies auflisten
./gradlew check --warning-mode all              # Mit allen Warnungen
```

### Frontend (pnpm)
```
cd frontend

# Entwicklung
pnpm start                                      # Dev-Server starten
pnpm build                                      # Production Build
pnpm build --configuration production           # Mit Optimierungen

# Testen
pnpm test                                       # Unit Tests
pnpm test -- --code-coverage                    # Mit Coverage-Report
pnpm e2e                                        # E2E Tests
pnpm e2e:ui                                     # E2E Tests mit UI-Feedback
pnpm e2e:install                                # Playwright-Browser und Abhängigkeiten installieren

# Code Qualität und Formatierung
pnpm lint                                       # ESLint ausführen
pnpm lintFix                                    # ESLint mit Auto-Fix
pnpm prettier                                   # Prettier ausführen
pnpm stylelint                                  # Stylelint mit Auto-Fix
```

---

## Datenbankzugriff

### H2 Console
Nach dem Backend-Start kannst du die Datenbank über die H2 Web Console ansehen: <br>
http://localhost:8080/api/h2-console

**Verbindung:**
- JDBC URL: `jdbc:h2:mem:tmws;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
- Benutzer: (Das zuvor festgelegte `DATASOURCE_USERNAME` in `application.local.properties`.)
- Passwort: (Das zuvor festgelegte `DATASOURCE_PASSWORD` in `application.local.properties`.)

### SQL Queries Beispiele
```
-- Alle Benutzer anzeigen
SELECT * FROM users;

-- Alle Projektboards eines Benutzers
SELECT * FROM boards WHERE owner_id = 1;

-- Aufgaben in einer Statuskategorie
SELECT * FROM tasks WHERE column_id = 1 ORDER BY position_index;
```

---

## Server-Health-Check
Um die Erreichbarkeit des Backends zu überprüfen, kannst du den folgenden Endpoint aufrufen: <br>
http://localhost:8080/api/health

---

## API-Endpoints

### Authentifizierung
```
POST    /api/auth/register                      # Registrierung
POST    /api/auth/login                         # Anmeldung
GET     /api/auth/verify-email                  # E-Mail verifizieren
POST    /api/auth/password-reset                # Passwort zurücksetzen (Anfrage)
PUT     /api/auth/reset-password                # Passwort zurücksetzen (Update)
```

### Benutzer
```
GET     /api/auth/me                            # Aktuellen Benutzer abrufen
PUT     /api/auth/profile                       # Profil aktualisieren
PUT     /api/auth/profile/change-password       # Passwort ändern
DELETE  /api/auth/me                            # Account deaktivieren
```

### Projektboard
```
GET     /api/boards                             # Alle Projektboards abrufen
POST    /api/boards                             # Neues Projektboard erstellen
GET     /api/boards/{id}                        # Projektboard-Details abrufen
PUT     /api/boards/{id}                        # Projektboard aktualisieren
```

### Statuskategorien
```
POST    /api/boards/{id}/columns                # Neue Statuskategorie erstellen
PUT     /api/boards/columns/{id}                # Statuskategorie aktualisieren
PUT     /api/boards/columns/{id}/move           # Statuskategorie verschieben (Position ändern)
DELETE  /api/boards/columns/{id}                # Statuskategorie löschen
```

### Aufgaben
```
POST    /api/boards/columns/{columnId}/tasks    # Neue Aufgabe erstellen
PUT     /api/boards/tasks/{taskId}              # Aufgabe aktualisieren
PUT     /api/boards/tasks/{taskId}/move         # Aufgabe verschieben (Position ändern)
DELETE  /api/boards/tasks/{taskId}              # Aufgabe löschen
```

### Projektboard Mitglieder
```
POST    /api/boards/{id}/invite                 # Benutzer zu Projektboard einladen
```

### Sonstiges

```
GET     /api/health                             # Health Check
```

---

## Fehlerbehebung

### Backend-Probleme

**Port 8080 bereits in Verwendung:**
```
# Anderen Port verwenden
./gradlew bootRun --args='--server.port=8081'
```

**H2 Console nicht erreichbar:**
- Überprüfe `application.properties`: `spring.h2.console.enabled=true`
- URL: http://localhost:8080/api/h2-console

**JWT-Fehler:**
- Überprüfe `jwt.secret` in `application.local.properties`
- Muss mindestens 32 Zeichen lang sein für HS256

### Frontend-Probleme

**Port 4200 bereits in Verwendung:**
```
pnpm start -- --port 4300
```

**Abhängigkeiten nicht installiert:**
```
rm -rf node_modules pnpm-lock.yaml
pnpm install
```

### Verbindungsprobleme

**Frontend kann Backend nicht erreichen:**
- Überprüfe CORS-Konfiguration
- Läuft Backend auf http://localhost:8080?
- Läuft Frontend auf http://localhost:4200?
- Überprüfe Frontend, Backend und Browser-Konsole auf Fehler

---

## Danksagung
Danke an [Slifer808](https://steamcommunity.com/profiles/76561198347469960) für die Hilfe beim Erstellen vom `favicon.ico`.

---

© 2026-2026 mexikoedi

Alle Rechte vorbehalten.
