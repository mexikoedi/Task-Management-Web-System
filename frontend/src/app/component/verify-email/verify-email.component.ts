/**
 * Diese Komponente ist für die Verifikation der E-Mail-Adresse eines Benutzers zuständig.
 */
import { Component, OnInit, inject, signal, WritableSignal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { NgIcon } from '@ng-icons/core';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, NgIcon],
  templateUrl: './verify-email.component.html',
  styleUrl: './verify-email.component.css',
})
export class VerifyEmailComponent implements OnInit {
  isLoading: WritableSignal<boolean> = signal(false);
  success: WritableSignal<boolean> = signal(false);
  successMessage: WritableSignal<string | null> = signal<string | null>(null);
  errorMessage: WritableSignal<string | null> = signal(null);
  readonly router: Router = inject(Router);
  private readonly route: ActivatedRoute = inject(ActivatedRoute);
  private readonly authService: AuthService = inject(AuthService);

  /**
   * Bei der Initialisierung der Komponente wird der Verifikationstoken aus den URL-Parametern extrahiert und
   * die Verifikationsmethode aufgerufen.
   * Wenn kein Token vorhanden ist, wird eine Fehlermeldung angezeigt.
   */
  ngOnInit(): void {
    this.route.queryParams.subscribe((params: Params): void => {
      const token: string | undefined = params['token'];

      if (!token) {
        this.errorMessage.set('Verifikationstoken fehlt.');

        return;
      }

      this.verifyEmail(token);
    });
  }

  /**
   * Diese Methode ruft den AuthService auf, um die E-Mail-Adresse mit dem bereitgestellten Token zu verifizieren.
   *
   * @param token Der Verifikationstoken, der in den URL-Parametern übergeben wurde.
   */
  private verifyEmail(token: string): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.authService.verifyEmail(token).subscribe({
      next: (): void => {
        this.success.set(true);
        this.isLoading.set(false);
        this.successMessage.set('E-Mail-Adresse erfolgreich verifiziert. Sie werden zur Anmeldung weitergeleitet...');

        setTimeout((): void => {
          this.successMessage.set(null);
          void this.router.navigate(['/login'], { queryParams: { verified: 'true' } });
        }, 4000);
      },
      error: (err: HttpErrorResponse): void => {
        this.handleError(err);
        this.isLoading.set(false);
      },
    });
  }

  /**
   * Wechselt zum Anmeldungsmodus und setzt Fehlermeldungen zurück.
   */
  switchToLogin(): void {
    void this.router.navigate(['/login']);
    this.errorMessage.set(null);
  }

  /**
   * Behandelt Fehler, die während der E-Mail-Verifikation auftreten können.
   * @param error Der Fehler, der aufgetreten ist, ein HttpErrorResponse von dem AuthService.
   */
  private handleError(error: HttpErrorResponse): void {
    this.errorMessage.set(error.error.message || 'Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.');

    return;
  }
}
