/**
 * Diese Service-Klasse bietet Methoden für die Authentifizierung und Benutzerverwaltung in der Angular-Anwendung.
 * Sie kommuniziert mit einem Backend-API, um Aktionen wie Login, Registrierung, Passwort-Reset, E-Mail-Verifizierung,
 * Profilaktualisierung und Kontodeaktivierung durchzuführen.
 * Die Methoden verwenden HTTP-Anfragen, um die entsprechenden Aktionen auf dem Server auszuführen und verwalten den
 * aktuellen Benutzerstatus in der Anwendung.
 */
import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { UserModel } from '../model/user.model';
import { LoginResponse, RegisterResponse } from '../model/auth-response.model';
import { Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthService {
  get currentUser$(): Observable<UserModel | null> {
    return this.currentUserSubject.asObservable();
  }

  private readonly API_URL: string = 'http://localhost:8080/api/auth';
  private readonly tokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(this.getToken());
  private readonly currentUserSubject: BehaviorSubject<UserModel | null> = new BehaviorSubject<UserModel | null>(null);
  private readonly http: HttpClient = inject(HttpClient);
  private readonly router: Router = inject(Router);

  /**
   * Diese Methode sendet eine POST-Anfrage an das Backend-API, um einen Benutzer mit der angegebenen E-Mail und
   * Passwort zu authentifizieren.
   *
   * @param email Die E-Mail-Adresse des Benutzers, der sich anmelden möchte.
   * @param password Das Passwort des Benutzers, der sich anmelden möchte.
   * @returns Ein Observable, das die Antwort des Login-Versuchs enthält, einschließlich des Tokens bei Erfolg.
   */
  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/login`, { email, password }).pipe(
      tap((response: LoginResponse): void => {
        if (response.token) {
          sessionStorage.setItem('token', response.token);
          this.tokenSubject.next(response.token);
        }
      })
    );
  }

  /**
   * Diese Methode sendet eine POST-Anfrage an das Backend-API, um einen neuen Benutzer mit den angegebenen
   * Informationen zu registrieren.
   *
   * @param name Der Name des Benutzers, der sich registrieren möchte.
   * @param email Die E-Mail-Adresse des Benutzers, der sich registrieren möchte.
   * @param password Das Passwort des Benutzers, der sich registrieren möchte.
   * @param passwordConfirm Das Passwort zur Bestätigung, dass der Benutzer das Passwort korrekt eingegeben hat.
   * @returns Ein Observable, das die Antwort des Registrierungsversuchs enthält, einschließlich des Tokens bei Erfolg.
   */
  register(name: string, email: string, password: string, passwordConfirm: string): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.API_URL}/register`, {
      name,
      email,
      password,
      passwordConfirm,
    });
  }

  /**
   * Diese Methode sendet eine POST-Anfrage an das Backend-API, um einen Passwort-Reset für die angegebene
   * E-Mail-Adresse anzufordern.
   *
   * @param email Die E-Mail-Adresse des Benutzers, für den der Passwort-Reset angefordert wird.
   * @returns Ein Observable, das die Antwort des Passwort-Reset-Versuchs enthält.
   */
  requestPasswordReset(email: string): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/password-reset`, { email });
  }

  /**
   * Diese Methode sendet eine GET-Anfrage an das Backend-API, um die E-Mail-Adresse eines Benutzers zu verifizieren,
   * indem ein Token übergeben wird.
   *
   * @param token Das Token, das zur Verifizierung der E-Mail-Adresse verwendet wird.
   * @returns Ein Observable, das die Antwort der E-Mail-Verifizierung enthält.
   */
  verifyEmail(token: string): Observable<void> {
    return this.http.get<void>(`${this.API_URL}/verify-email`, { params: { token } });
  }

  /**
   * Diese Methode sendet eine PUT-Anfrage an das Backend-API, um das Passwort eines Benutzers zurückzusetzen,
   * indem ein Token und das neue Passwort übergeben werden.
   *
   * @param token Das Token, das zur Authentifizierung des Passwort-Reset-Vorgangs verwendet wird.
   * @param password Das neue Passwort, das für den Benutzer festgelegt werden soll.
   * @returns Ein Observable, das die Antwort des Passwort-Reset-Versuchs enthält.
   */
  resetPassword(token: string, password: string): Observable<void> {
    return this.http.put<void>(`${this.API_URL}/reset-password`, { password }, { params: { token } });
  }

  /**
   * Diese Methode sendet eine PUT-Anfrage an das Backend-API, um das Profil eines Benutzers zu aktualisieren,
   * indem der Name, die neue E-Mail-Adresse und das Profilbild übergeben werden.
   *
   * @param name Der neue Name des Benutzers, der aktualisiert werden soll.
   * @param newEmail Die neue E-Mail-Adresse des Benutzers, die aktualisiert werden soll.
   * @param image Das neue Profilbild des Benutzers, das aktualisiert werden soll.
   * @returns Ein Observable, das die aktualisierten Benutzerdaten enthält.
   */
  updateProfile(name?: string, newEmail?: string, image?: string): Observable<UserModel> {
    return this.http
      .put<UserModel>(`${this.API_URL}/profile`, {
        name,
        newEmail,
        image,
      })
      .pipe(
        tap((user: UserModel): void => {
          this.currentUserSubject.next(user);
        })
      );
  }

  /**
   * Diese Methode sendet eine PUT-Anfrage an das Backend-API, um das Passwort eines Benutzers zu ändern,
   * indem das aktuelle Passwort, das neue Passwort und die Bestätigung des neuen Passworts übergeben werden.
   *
   * @param currentPassword Das aktuelle Passwort des Benutzers.
   * @param newPassword Das neue Passwort, das für den Benutzer festgelegt werden soll.
   * @param newPasswordConfirm Die Bestätigung des neuen Passworts.
   * @return Ein Observable, das die Antwort des Passwortänderungsversuchs enthält.
   */
  changePassword(currentPassword?: string, newPassword?: string, newPasswordConfirm?: string): Observable<void> {
    return this.http.put<void>(`${this.API_URL}/profile/change-password`, {
      currentPassword,
      newPassword,
      newPasswordConfirm,
    });
  }

  /**
   * Diese Methode sendet eine DELETE-Anfrage an das Backend-API, um das Konto eines Benutzers zu deaktivieren,
   *
   * @param email Die E-Mail-Adresse des Benutzers, dessen Konto deaktiviert werden soll.
   * @returns Ein Observable, das die Antwort der Kontodeaktivierung enthält.
   */
  deactivateAccount(email: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/me`, { params: { email } });
  }

  /**
   * Diese Methode sendet eine GET-Anfrage an das Backend-API, um die Daten des aktuell authentifizierten
   * Benutzers abzurufen.
   *
   * @returns Ein Observable, das die Daten des aktuellen Benutzers enthält.
   */
  getCurrentUser(): Observable<UserModel> {
    return this.http
      .get<UserModel>(`${this.API_URL}/me`)
      .pipe(tap((user: UserModel): void => this.currentUserSubject.next(user)));
  }

  /**
   * Diese Methode gibt den aktuellen Benutzerstatus als synchronen Snapshot zurück, indem sie den aktuellen Wert
   * des currentUserSubject-BehaviorSubject abruft.
   *
   * @return Der aktuelle Benutzer als UserModel-Objekt, wenn ein Benutzer authentifiziert ist, oder null, wenn kein
   * Benutzer authentifiziert ist.
   */
  getCurrentUserSnapshot(): UserModel | null {
    return this.currentUserSubject.value;
  }

  /**
   * Diese Methode entfernt das Authentifizierungs-Token aus dem Session Storage und setzt den aktuellen Benutzer
   * und das Token auf null.
   * Dadurch wird der Benutzer ausgeloggt und alle Authentifizierungsinformationen werden gelöscht.
   */
  logout(): void {
    sessionStorage.removeItem('token');
    this.tokenSubject.next(null);
    this.router.navigate(['/login']).then();
  }

  /**
   * Diese Methode überprüft, ob ein gültiges Authentifizierungs-Token vorhanden ist,
   * indem sie die getToken-Methode aufruft und prüft, ob ein Token zurückgegeben wird.
   *
   * @returns Ein boolean-Wert, der angibt, ob der Benutzer authentifiziert ist oder nicht.
   */
  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  /**
   * Diese Methode ruft das Authentifizierungs-Token aus dem Session Storage ab, indem sie den Schlüssel 'token' verwendet.
   *
   * @returns Das Authentifizierungs-Token als string, wenn es vorhanden ist, oder null, wenn kein Token gefunden wird.
   */
  getToken(): string | null {
    return sessionStorage.getItem('token');
  }

  /**
   * Diese Methode extrahiert die E-Mail-Adresse eines Benutzers aus dem JWT-Token, das im Session Storage gespeichert ist.
   * Sie ruft das Token ab, dekodiert den Payload-Teil des Tokens (der die Benutzerdaten enthält) und versucht,
   * die E-Mail-Adresse entweder aus dem 'sub'-Feld oder dem 'email'-Feld zu extrahieren.
   *
   * @returns Die E-Mail-Adresse des Benutzers als string, wenn sie erfolgreich extrahiert werden kann, oder null,
   * wenn kein gültiges Token gefunden wird oder die E-Mail-Adresse nicht im Token enthalten ist.
   */
  getEmailFromToken(): string | null {
    const token: string | null = this.getToken();
    if (!token) return null;

    try {
      const parts: string[] = token.split('.');
      if (parts.length < 2) return null;
      const payload: string = parts[1]!;
      let b64: string = payload.replace(/-/g, '+').replace(/_/g, '/');
      while (b64.length % 4) b64 += '=';
      const json = JSON.parse(atob(b64)) as { sub?: string; email?: string };

      return json.sub ?? json.email ?? null;
    } catch {
      return null;
    }
  }
}
