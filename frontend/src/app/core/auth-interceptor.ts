/**
 * Interceptor, der automatisch den JWT-Token an alle HTTP-Anfragen anhängt, außer an paar
 * Authentifizierungsendpunkte, die keinen Token benötigen.
 * Außerdem behandelt er 401-Fehler, indem er den Benutzer automatisch ausloggt und zum Login weiterleitet.
 * Dies stellt sicher, dass alle geschützten Endpunkte mit einem gültigen Token aufgerufen werden und verbessert die
 * Sicherheit der Anwendung.
 */
import { inject } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { catchError, Observable, throwError } from 'rxjs';
import { AuthService } from '../service/auth.service';

export const authInterceptor: HttpInterceptorFn = (
  request: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
  const authService: AuthService = inject(AuthService);

  if (
    request.url.startsWith('/api/auth/login') ||
    request.url.startsWith('/api/auth/register') ||
    request.url.startsWith('/api/auth/password-reset') ||
    request.url.startsWith('/api/auth/verify-email') ||
    request.url.startsWith('/api/auth/reset-password')
  ) {
    return next(request);
  }

  const token: string | null = authService.getToken();

  if (token) {
    request = request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }

  return next(request).pipe(
    catchError((error: HttpErrorResponse): Observable<never> => {
      if (error.status === 401) {
        authService.logout();

        return new Observable<never>();
      }

      return throwError((): HttpErrorResponse => error);
    })
  );
};
