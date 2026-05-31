/**
 * Diese Datei ist der Haupteinstiegspunkt der Angular-Anwendung.
 * Hier wird die Anwendung mit der `bootstrapApplication`-Funktion gestartet, die das `AppComponent`
 * als Root-Komponente verwendet und die Konfiguration aus `appConfig` lädt.
 */
import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { appConfig } from './app/app.config';

bootstrapApplication(AppComponent, appConfig).catch((err: unknown): void => console.error(err));
