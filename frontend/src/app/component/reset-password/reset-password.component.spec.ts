/**
 * Diese Datei enthält Unit-Tests für die ResetPasswordComponent der Angular-Anwendung.
 * Sie verwendet das Angular-Testframework und Vitest, um eine Test-Suite zu erstellen, die verschiedene Aspekte der
 * ResetPasswordComponent überprüft.
 * Die Tests umfassen die Überprüfung der Initialisierung, der Formularvalidierung, der Interaktion mit dem AuthService
 * und der Navigation.
 */
import { describe, it, expect, vi, beforeEach, Mock } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { ResetPasswordComponent } from './reset-password.component';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { of, throwError, type Observable } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { provideIcons } from '@ng-icons/core';
import { bootstrapCheckLg } from '@ng-icons/bootstrap-icons';
import { FormUtilsShared } from '../../shared/form-utils.shared';

describe('ResetPasswordComponent', (): void => {
  let component: ResetPasswordComponent;

  const routerMock: Partial<Router> = {
    navigate: vi.fn(),
  };

  const resetPasswordMock: Mock = vi.fn<(token: string, password: string) => Observable<void>>();

  const authServiceMock: Partial<AuthService> = {
    resetPassword: resetPasswordMock,
  };

  const formUtilsMock: Partial<FormUtilsShared> = {
    passwordStrengthValidator: vi.fn((): (() => null) => (): null => null),
    setupAutoClearErrors: vi.fn(),
    updateFieldErrors: vi.fn(),
    getErrorMessage: vi.fn((): string => 'Fehler'),
    markFormGroupTouched: vi.fn(),
  };

  const createRouteMock: (params: Record<string, string>) => Partial<ActivatedRoute> = (
    params: Record<string, string>
  ): Partial<ActivatedRoute> => ({
    queryParams: of(params),
  });

  const createComponent: (params: Record<string, string>) => void = (params: Record<string, string>): void => {
    TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: Router, useValue: routerMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: ActivatedRoute, useValue: createRouteMock(params) },
        { provide: FormUtilsShared, useValue: formUtilsMock },
        provideIcons({ bootstrapCheckLg }),
      ],
    });

    component = TestBed.createComponent(ResetPasswordComponent).componentInstance;
  };

  beforeEach((): void => {
    vi.clearAllMocks();
  });

  it('Setzt errorMessage und deaktiviert Formular, wenn Token fehlt', (): void => {
    createComponent({});
    component.ngOnInit();
    expect(component.errorMessage()).toBe('Passwort-Reset-Token fehlt.');
    expect(component.resetForm.disabled).toBe(true);
  });

  it('Aktiviert Formular, wenn Token vorhanden ist', (): void => {
    createComponent({ token: 'abc123' });
    component.ngOnInit();
    expect(component.token).toBe('abc123');
    expect(component.resetForm.disabled).toBe(false);
  });

  it('Bricht ab, wenn Formular ungültig ist', (): void => {
    createComponent({ token: 'abc123' });
    component.ngOnInit();
    component.resetForm.controls.password.setValue('');
    component.onResetPassword();
    expect(formUtilsMock.markFormGroupTouched).toHaveBeenCalled();
    expect(resetPasswordMock).not.toHaveBeenCalled();
  });

  it('Ruft resetPassword auf, wenn Formular gültig ist', (): void => {
    resetPasswordMock.mockReturnValue(of(void 0));
    createComponent({ token: 'abc123' });
    component.ngOnInit();
    component.resetForm.controls.password.setValue('Passwort123');
    component.onResetPassword();
    expect(authServiceMock.resetPassword).toHaveBeenCalledWith('abc123', 'Passwort123');
  });

  it('Setzt successMessage und navigiert nach 4 Sekunden', (): void => {
    vi.useFakeTimers();
    resetPasswordMock.mockReturnValue(of(void 0));
    createComponent({ token: 'abc123' });
    component.ngOnInit();
    component.resetForm.controls.password.setValue('Passwort123');
    component.onResetPassword();
    expect(component.success()).toBe(true);
    expect(component.successMessage()).toContain('erfolgreich zurückgesetzt');
    vi.advanceTimersByTime(4000);
    expect(routerMock.navigate).toHaveBeenCalledWith(['/login']);
    vi.useRealTimers();
  });

  it('Setzt errorMessage bei Fehler vom Server', (): void => {
    resetPasswordMock.mockReturnValue(
      throwError(
        (): HttpErrorResponse =>
          new HttpErrorResponse({
            status: 400,
            error: { message: 'Token ungültig' },
          })
      )
    );

    createComponent({ token: 'abc123' });
    component.ngOnInit();
    component.resetForm.controls.password.setValue('Passwort123');
    component.onResetPassword();
    expect(component.errorMessage()).toBe('Token ungültig');
  });

  it('TogglePasswordVisibility ändert showPassword korrekt', (): void => {
    createComponent({ token: 'abc123' });
    expect(component.showPassword).toBe(false);
    component.togglePasswordVisibility();
    expect(component.showPassword).toBe(true);
    component.togglePasswordVisibility();
    expect(component.showPassword).toBe(false);
  });

  it('SwitchToLogin setzt Formular zurück und navigiert', (): void => {
    createComponent({ token: 'abc123' });
    component.switchToLogin();
    expect(routerMock.navigate).toHaveBeenCalledWith(['/login']);
    expect(component.errorMessage()).toBe(null);
    expect(component.resetForm.untouched).toBe(true);
  });
});
