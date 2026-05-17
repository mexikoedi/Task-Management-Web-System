import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import {UserSummary} from "../model/board.model";

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = 'http://localhost:8080/api/auth';
  private tokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(
    this.getToken()
  );
  public token$ = this.tokenSubject.asObservable();
  private currentUserSubject = new BehaviorSubject<UserSummary | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

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
      // JWT payload is the second part (base64url). Decode and parse JSON.
      const parts = token.split('.');
      if (parts.length < 2) return null;
      const payload = parts[1];
      // base64url -> base64
      let b64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      while (b64.length % 4) b64 += '=';
      const json = JSON.parse(atob(b64));
      return (json.sub as string) || (json.email as string) || null;
    } catch {
      return null;
    }
  }

  /** Lösche den Account des aktuell eingeloggten Users (über Email query param) */
  deactivateAccount(email: string) {
    return this.http.delete(`${this.API_URL}/me`, { params: { email } });
  }

  /** Hol den aktuellen User (Email query param) */
  getCurrentUser(email: string): Observable<UserSummary> {
    return this.http.get<UserSummary>(`${this.API_URL}/me`, { params: { email } });
  }

  /** Update Profil-Informationen (Name) */
  updateProfile(
    name?: string,
    newEmail?: string,
    image?: string,
    currentPassword?: string,
    newPassword?: string,
    newPasswordConfirm?: string
  ): Observable<UserSummary> {
    return this.http
      .put<UserSummary>(`${this.API_URL}/profile`, { name, newEmail, image, currentPassword, newPassword, newPasswordConfirm })
      .pipe(
        tap((user) => {
          // Aktuellen User aktualisieren
          this.currentUserSubject.next(user);
        })
      );
  }

  /** Lädt aktuellen User und setzt BehaviorSubject */
  loadCurrentUser(email: string): Observable<UserSummary> {
    return this.getCurrentUser(email).pipe(
      tap((user: UserSummary) => {
        this.currentUserSubject.next(user);
      })
    );
  }

  /** Liefert den aktuellen User synchron (falls nötig) */
  getCurrentUserSnapshot(): UserSummary | null {
    return this.currentUserSubject.value;
  }

  /** Ändere das Passwort */
  changePassword(
    email: string,
    currentPassword: string,
    newPassword: string,
    newPasswordConfirm: string
  ) {
    return this.http.put(
      `${this.API_URL}/change-password`,
      { currentPassword, newPassword, newPasswordConfirm },
      { params: { email } }
    );
  }
}

