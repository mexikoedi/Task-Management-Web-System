/**
 * Diese Datei enthält Unit-Tests für die VerifyEmailComponent der Angular-Anwendung.
 * Sie verwendet das Angular-Testframework und Vitest, um eine Test-Suite zu erstellen, die verschiedene Szenarien der
 * E-Mail-Verifikation überprüft.
 * Die Tests stellen sicher, dass die Komponente korrekt auf fehlende Tokens reagiert, die Verifikationsmethode des
 * AuthService aufruft, Erfolgsmeldungen anzeigt und Fehler behandelt.
 */
import { describe, it, expect, vi, beforeEach, Mock } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { VerifyEmailComponent } from './verify-email.component';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { Observable, of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { provideIcons } from '@ng-icons/core';
import { bootstrapCheckLg } from '@ng-icons/bootstrap-icons';

describe('VerifyEmailComponent', (): void => {
  let component: VerifyEmailComponent;
  const routerMock: { navigate: Mock } = {
    navigate: vi.fn(),
  } satisfies Partial<Router>;

  const authServiceMock: { verifyEmail: Mock } = {
    verifyEmail: vi.fn(),
  } satisfies Partial<AuthService>;

  const createRouteMock: (params: Record<string, string>) => { queryParams: Observable<Record<string, string>> } = (
    params: Record<string, string>
  ): { queryParams: Observable<Record<string, string>> } =>
    ({
      queryParams: of(params),
    }) satisfies Partial<ActivatedRoute>;

  const createComponent: (params: Record<string, string>) => void = (params: Record<string, string>): void => {
    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: routerMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: ActivatedRoute, useValue: createRouteMock(params) },
        provideIcons({ bootstrapCheckLg }),
      ],
    });

    component = TestBed.createComponent(VerifyEmailComponent).componentInstance;
  };

  beforeEach((): void => {
    vi.clearAllMocks();
  });

  it('Setzt errorMessage, wenn kein Token vorhanden ist', (): void => {
    createComponent({});
    component.ngOnInit();
    expect(component.errorMessage()).toBe('Verifikationstoken fehlt.');
  });

  it('Ruft verifyEmail auf, wenn Token vorhanden ist', (): void => {
    authServiceMock.verifyEmail.mockReturnValue(of({}));
    createComponent({ token: 'abc123' });
    component.ngOnInit();
    expect(authServiceMock.verifyEmail).toHaveBeenCalledWith('abc123');
  });

  it('Setzt success & successMessage bei erfolgreicher Verifikation', (): void => {
    authServiceMock.verifyEmail.mockReturnValue(of({}));
    createComponent({ token: 'abc123' });
    component.ngOnInit();
    expect(component.success()).toBe(true);
    expect(component.successMessage()).toContain('erfolgreich verifiziert');
  });

  it('Navigiert nach 4 Sekunden zum Login', (): void => {
    vi.useFakeTimers();
    authServiceMock.verifyEmail.mockReturnValue(of({}));
    createComponent({ token: 'abc123' });
    component.ngOnInit();
    vi.advanceTimersByTime(4000);
    expect(routerMock.navigate).toHaveBeenCalledWith(['/login'], { queryParams: { verified: 'true' } });
    vi.useRealTimers();
  });

  it('Setzt errorMessage bei Fehler vom Server', (): void => {
    authServiceMock.verifyEmail.mockReturnValue(
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
    expect(component.errorMessage()).toBe('Token ungültig');
  });
});
