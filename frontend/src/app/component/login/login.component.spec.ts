/**
 * Diese Datei enthält Unit-Tests für die LoginComponent der Angular-Anwendung.
 * Sie verwendet das Angular-Testframework und Vitest, um eine Test-Suite zu erstellen, die verschiedene Aspekte der
 * LoginComponent überprüft.
 * Die Tests umfassen die Überprüfung der Initialisierung, der Verarbeitung von Query-Parametern, der Formularvalidierung,
 * der Interaktion mit dem AuthService und der Navigation.
 */
import { describe, it, expect, beforeEach, vi, Mock } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { Router, ActivatedRoute } from '@angular/router';
import { of, throwError, Subject } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { FormUtilsShared } from '../../shared/form-utils.shared';
import { AuthService } from '../../service/auth.service';

describe('LoginComponent', (): void => {
  let component: LoginComponent;
  let authServiceMock: { login: Mock; register: Mock; requestPasswordReset: Mock };
  let routerMock: { navigate: Mock };
  let activatedRouteMock: { queryParams: Subject<Record<string, string>> };
  let formUtilsMock: Partial<FormUtilsShared>;

  beforeEach((): void => {
    authServiceMock = {
      login: vi.fn(),
      register: vi.fn(),
      requestPasswordReset: vi.fn(),
    };

    routerMock = {
      navigate: vi.fn((): Promise<boolean> => Promise.resolve(true)),
    };

    activatedRouteMock = {
      queryParams: new Subject<Record<string, string>>(),
    };

    formUtilsMock = {
      emailValidator: vi.fn((): (() => null) => (): null => null),
      passwordStrengthValidator: vi.fn((): (() => null) => (): null => null),
      setupAutoClearErrors: vi.fn(),
      updateFieldErrors: vi.fn(),
      getErrorMessage: vi.fn((): string => 'Fehler'),
      markFormGroupTouched: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock },
        { provide: ActivatedRoute, useValue: activatedRouteMock },
        { provide: FormUtilsShared, useValue: formUtilsMock },
      ],
    });

    component = TestBed.createComponent(LoginComponent).componentInstance;
    component.ngOnInit();
  });

  it('Sollte korrekt initialisiert werden', (): void => {
    expect(component).toBeTruthy();
    expect(component.mode).toBe('login');
  });

  it('Sollte Query-Parameter "verified=true" verarbeiten', (): void => {
    activatedRouteMock.queryParams.next({ verified: 'true' });
    expect(component.successMessage()).toBe('E-Mail erfolgreich verifiziert! Sie können sich jetzt anmelden.');
    expect(component.mode).toBe('login');
  });

  it('Sollte Query-Parameter "reset=true" verarbeiten', (): void => {
    activatedRouteMock.queryParams.next({ reset: 'true' });
    expect(component.successMessage()).toBe(
      'Link zum Zurücksetzen des Passworts wurde an Ihre E-Mail gesendet. Klicken Sie auf den Link, um Ihr Passwort zurückzusetzen.'
    );
    expect(component.mode).toBe('reset');
  });

  it('Login-Formular sollte ungültig sein, wenn leer', (): void => {
    component.loginForm.setValue({ email: '', password: '' });
    expect(component.isLoginFormValid()).toBe(false);
  });

  it('Register-Formular sollte ungültig sein, wenn Passwörter nicht übereinstimmen', (): void => {
    component.registerForm.setValue({
      name: 'Max',
      email: 'test@test.de',
      password: 'Passwort123!',
      passwordConfirm: 'Falsch',
    });

    expect(component.isRegisterFormValid()).toBe(false);
  });

  it('Sollte Passwortsichtbarkeit toggeln', (): void => {
    expect(component.showPassword).toBe(false);
    component.togglePasswordVisibility();
    expect(component.showPassword).toBe(true);
  });

  it('Sollte Passwortbestätigungssichtbarkeit toggeln', (): void => {
    expect(component.showPasswordConfirm).toBe(false);
    component.togglePasswordConfirmVisibility();
    expect(component.showPasswordConfirm).toBe(true);
  });

  it('Sollte Login abbrechen, wenn Formular ungültig ist', (): void => {
    component.loginForm.setValue({ email: '', password: '' });
    component.onLogin();
    expect(formUtilsMock.markFormGroupTouched).toHaveBeenCalled();
    expect(authServiceMock.login).not.toHaveBeenCalled();
  });

  it('Sollte erfolgreichen Login verarbeiten', async (): Promise<void> => {
    vi.useFakeTimers();
    component.loginForm.setValue({ email: 'test@test.de', password: '123456' });
    authServiceMock.login.mockReturnValue(of({}));
    component.onLogin();
    expect(component.isLoading()).toBe(true);
    expect(authServiceMock.login).toHaveBeenCalledWith('test@test.de', '123456');
    vi.runAllTimers();
    await Promise.resolve();
    expect(component.successMessage()).toBeNull();
    expect(routerMock.navigate).toHaveBeenCalledWith(['/dashboard']);
    expect(component.isLoading()).toBe(false);
    vi.useRealTimers();
  });

  it('Sollte Login-Fehler verarbeiten', (): void => {
    const error = new HttpErrorResponse({ error: { message: 'Login fehlgeschlagen' }, status: 400 });
    authServiceMock.login.mockReturnValue(throwError((): HttpErrorResponse => error));
    component.loginForm.setValue({ email: 'test@test.de', password: '123456' });
    component.onLogin();
    expect(component.errorMessage()).toBe('Login fehlgeschlagen');
    expect(component.isLoading()).toBe(false);
  });

  it('Sollte zu Register wechseln', (): void => {
    component.switchToRegister();
    expect(component.mode).toBe('register');
    expect(component.errorMessage()).toBeNull();
    expect(component.successMessage()).toBeNull();
  });

  it('Sollte zu Reset wechseln', (): void => {
    component.switchToReset();
    expect(component.mode).toBe('reset');
    expect(component.errorMessage()).toBeNull();
    expect(component.successMessage()).toBeNull();
  });

  it('Sollte zu Login wechseln', (): void => {
    component.switchToLogin();
    expect(component.mode).toBe('login');
    expect(component.errorMessage()).toBeNull();
    expect(component.successMessage()).toBeNull();
  });
});
