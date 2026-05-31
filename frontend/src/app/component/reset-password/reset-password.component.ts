/**
 * ResetPasswordComponent ist eine Angular-Komponente, die es Benutzern ermöglicht, ihr Passwort zurückzusetzen.
 */
import { Component, OnInit, inject, WritableSignal, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormControl } from '@angular/forms';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { NgIcon } from '@ng-icons/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormUtilsShared } from '../../shared/form-utils.shared';

interface ResetFormControls {
  password: FormControl<string>;
}

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgIcon],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.css',
})
export class ResetPasswordComponent implements OnInit {
  fb: FormBuilder = inject(FormBuilder);
  formUtilsShared: FormUtilsShared = inject(FormUtilsShared);
  resetForm: FormGroup<ResetFormControls> = this.fb.nonNullable.group({
    password: [
      '',
      [
        Validators.required,
        Validators.minLength(8),
        Validators.maxLength(50),
        this.formUtilsShared.passwordStrengthValidator(),
      ],
    ],
  });
  token = '';
  showPassword = false;
  fieldErrors: Record<string, string[]> = {};
  isLoading: WritableSignal<boolean> = signal(false);
  success: WritableSignal<boolean> = signal(false);
  successMessage: WritableSignal<string | null> = signal<string | null>(null);
  errorMessage: WritableSignal<string | null> = signal<string | null>(null);
  readonly router: Router = inject(Router);
  private readonly route: ActivatedRoute = inject(ActivatedRoute);
  private readonly authService: AuthService = inject(AuthService);

  /**
   * Initialisiert die Komponente, indem sie die URL-Parameter überprüft und das Passwort-Reset-Token extrahiert.
   */
  ngOnInit(): void {
    this.route.queryParams.subscribe((params: Params): void => {
      this.token = params['token'];

      if (!this.token) {
        this.errorMessage.set('Passwort-Reset-Token fehlt.');
        this.resetForm.disable();

        return;
      }
    });

    this.formUtilsShared.setupAutoClearErrors(this.resetForm, (form: FormGroup, field: string): void =>
      this.formUtilsShared.updateFieldErrors(
        form,
        field,
        this.fieldErrors,
        this.formUtilsShared.getErrorMessage.bind(this.formUtilsShared)
      )
    );
  }

  /**
   * Verarbeitet die Passwort-Reset-Anfrage, indem es die Formulardaten validiert und an den AuthService sendet.
   */
  onResetPassword(): void {
    if (this.resetForm.invalid) {
      this.formUtilsShared.markFormGroupTouched(
        this.resetForm,
        this.fieldErrors,
        this.formUtilsShared.getErrorMessage.bind(this.formUtilsShared)
      );

      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.fieldErrors = {};
    const password: string = this.resetForm.controls.password.value;
    this.resetForm.disable();
    this.authService.resetPassword(this.token, password).subscribe({
      next: (): void => {
        this.success.set(true);
        this.successMessage.set('Passwort erfolgreich zurückgesetzt. Sie werden zur Anmeldung weitergeleitet...');

        setTimeout((): void => {
          this.resetForm.reset();
          this.successMessage.set(null);
          this.isLoading.set(false);
          void this.router.navigate(['/login']);
          this.resetForm.enable();
        }, 4000);
      },
      error: (err: HttpErrorResponse): void => {
        this.handleError(err);
        this.isLoading.set(false);
        this.resetForm.enable();
      },
    });
  }

  /**
   * Wechselt die Sichtbarkeit des Passworts im Eingabefeld.
   */
  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  /**
   * Überprüft, ob das Formular zum Zurücksetzen des Passworts gültig ist.
   *
   * @returns true, wenn das Formular zum Zurücksetzen des Passworts gültig ist, andernfalls false.
   */
  isResetFormValid(): boolean {
    return this.resetForm.valid;
  }

  /**
   * Wechselt zum Anmeldungsmodus, setzt Fehlermeldungen zurück, entfernt Fehlerzustände,
   * markiert die Formulare als unberührt, setzt die Formulare zurück und aktiviert sie wieder.
   */
  switchToLogin(): void {
    void this.router.navigate(['/login']);
    this.errorMessage.set(null);
    this.fieldErrors = {};
    this.resetForm.markAsUntouched();
    this.resetForm.reset();
    this.resetForm.enable();
  }

  /**
   * Behandelt Fehler, die während der Passwortzurücksetzung auftreten können.
   *
   * @param error Der Fehler, der aufgetreten ist, ein HttpErrorResponse von dem AuthService.
   */
  private handleError(error: HttpErrorResponse): void {
    this.fieldErrors = {};
    this.errorMessage.set(error.error.message || 'Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.');

    return;
  }
}
