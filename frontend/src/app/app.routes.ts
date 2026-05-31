/**
 * Diese Datei definiert die Routen für die Angular-Anwendung.
 * Sie importiert die notwendigen Komponenten und Guards und richtet die Routing-Konfiguration ein.
 * Die Routen umfassen Pfade für Login, E-Mail-Verifizierung, Passwort-Reset und eine geschützte Dashboard-Route,
 * die Authentifizierung erfordert.
 * Zusätzlich gibt es eine Wildcard-Route, um alle undefinierten Pfade zurück zur Login-Seite umzuleiten.
 */
import { Routes } from '@angular/router';
import { LoginComponent } from './component/login/login.component';
import { authGuard } from './core/auth-guard';
import { DashboardComponent } from './component/dashboard/dashboard.component';
import { VerifyEmailComponent } from './component/verify-email/verify-email.component';
import { ResetPasswordComponent } from './component/reset-password/reset-password.component';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },
  {
    path: 'login',
    component: LoginComponent,
  },
  {
    path: 'verify-email',
    component: VerifyEmailComponent,
  },
  {
    path: 'reset-password',
    component: ResetPasswordComponent,
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard],
  },
  {
    path: '**',
    redirectTo: 'login',
  },
];
