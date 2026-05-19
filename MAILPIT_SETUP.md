# Mailpit Installation und Setup

Mailpit ist ein Fake SMTP-Server für Entwicklung, mit dem E-Mails nicht wirklich versandt werden, sondern lokal gehostet und in einer Web-UI angesehen werden können.

## Installation

### Option 1: Docker
```bash
docker run -d -p 1025:1025 -p 8025:8025 --name mailpit axllent/mailpit
```

### Option 2: Direkter Download
1. Lade Mailpit herunter von: https://github.com/axllent/mailpit/releases
2. Entpacke die Datei
3. Starte Mailpit:
   ```bash
   ./mailpit  # macOS/Linux
   mailpit.exe  # Windows
   ```

## Ports
- **SMTP**: 1025 (Backend sendet E-Mails hierher)
- **Web-UI**: 8025 (Angezeigte E-Mails hier anschauen)

## Verwendung mit TMWS

Die `application.properties` ist bereits konfiguriert:
```properties
spring.mail.host=localhost
spring.mail.port=1025
spring.mail.username=
spring.mail.password=
spring.mail.properties.mail.smtp.auth=false
spring.mail.properties.mail.smtp.starttls.enable=false
```

## E-Mails anschauen

Nach Registrierung oder Passwort-Reset  besuche:
http://localhost:8025

Dort siehst du alle versandten E-Mails (Registrierung, Passwort-Reset, etc.)

---

**Letzte Aktualisierung:** 2025-05-19
