/**
 * AuthGuard sorgt dafür, dass nur authentifizierte Benutzer auf bestimmte Routen zugreifen können.
 * Wenn ein Benutzer nicht authentifiziert ist, wird er zur Login-Seite weitergeleitet.
 * Die aktuelle URL wird als Rückkehr-URL übergeben, damit der Benutzer nach erfolgreichem Login zurück zur
 * ursprünglichen Seite navigieren kann.
 */
import { inject } from '@angular/core';
import { Router, CanActivateFn, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AuthService } from '../service/auth.service';

export const authGuard: CanActivateFn = (
  _route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
): true | UrlTree => {
  const authService: AuthService = inject(AuthService);
  const router: Router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};
