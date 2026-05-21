import { Routes } from '@angular/router';
import { LoginComponent } from './page/login/login.component';
import { AuthGuard } from './core/guards/auth.guard';
import { DashboardComponent } from './page/dashboard/dashboard.component';
import { VerifyEmailComponent } from './page/verify-email/verify-email.component';
import { ResetPasswordComponent } from './page/reset-password/reset-password.component';

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
    canActivate: [AuthGuard],
  },
  {
    path: '**',
    redirectTo: 'login',
  },
];
