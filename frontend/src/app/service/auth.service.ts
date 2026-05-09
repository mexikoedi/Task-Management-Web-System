import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = 'http://localhost:8080/api/auth';
  private tokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(
    this.getToken()
  );
  public token$ = this.tokenSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Anmeldung mit Email und Passwort
   */
  login(email: string, password: string): Observable<any> {
    return this.http.post(`${this.API_URL}/login`, { email, password }).pipe(
      tap((response: any) => {
        if (response.token) {
          localStorage.setItem('token', response.token);
          this.tokenSubject.next(response.token);
        }
      })
    );
  }

  /**
   * Registrierung mit Name, Email und Passwort
   */
  register(name: string, email: string, password: string, passwordConfirm: string): Observable<any> {
    return this.http.post(`${this.API_URL}/register`, {
      name,
      email,
      password,
      passwordConfirm
    });
  }

  /**
   * Passwort zurücksetzen (via Email)
   */
  requestPasswordReset(email: string): Observable<any> {
    return this.http.post(`${this.API_URL}/password-reset`, { email });
  }

  /**
   * Email mit Token verifizieren
   */
  verifyEmail(token: string): Observable<any> {
    return this.http.get(`${this.API_URL}/verify-email`, { params: { token } }).pipe(
      tap(() => {
        console.log('Email verified successfully');
      })
    );
  }

  /**
   * Passwort mit Token zurücksetzen
   */
  resetPassword(token: string, password: string): Observable<any> {
    return this.http.put(`${this.API_URL}/reset-password`, { password }, { params: { token } });
  }

  /**
   * Logout
   */
  logout(): void {
    localStorage.removeItem('token');
    this.tokenSubject.next(null);
  }

  /**
   * Hol den Token aus dem Local Storage
   */
  getToken(): string | null {
    return localStorage.getItem('token');
  }

  /**
   * Check ob User authentifiziert ist
   */
  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  /**
   * Extrahiere Email aus Token
   */
  getEmailFromToken(): string | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const decoded = atob(token);
      return decoded.split(':')[0];
    } catch {
      return null;
    }
  }
}

