/**
 * Diese Komponente bietet eine Benutzeroberfläche für die Anmeldung, Registrierung und Passwortzurücksetzung.
 */
import { ChangeDetectionStrategy, Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { NgIcon } from '@ng-icons/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormUtilsShared } from '../../shared/form-utils.shared';

interface LoginFormControls {
  email: FormControl<string>;
  password: FormControl<string>;
}

interface RegisterFormControls {
  name: FormControl<string>;
  email: FormControl<string>;
  password: FormControl<string>;
  passwordConfirm: FormControl<string>;
}

interface ResetFormControls {
  email: FormControl<string>;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgIcon],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent implements OnInit {
  formUtilsShared: FormUtilsShared = inject(FormUtilsShared);
  fb: FormBuilder = inject(FormBuilder);
  loginForm: FormGroup<LoginFormControls> = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.maxLength(30), this.formUtilsShared.emailValidator()]],
    password: ['', [Validators.required]],
  });
  registerForm: FormGroup<RegisterFormControls> = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(30)]],
    email: ['', [Validators.required, Validators.maxLength(30), this.formUtilsShared.emailValidator()]],
    password: [
      '',
      [
        Validators.required,
        Validators.minLength(8),
        Validators.maxLength(50),
        this.formUtilsShared.passwordStrengthValidator(),
      ],
    ],
    passwordConfirm: ['', [Validators.required, this.passwordMatchValidator()]],
  });
  passwordResetForm: FormGroup<ResetFormControls> = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.maxLength(30), this.formUtilsShared.emailValidator()]],
  });
  mode: 'login' | 'register' | 'reset' = 'login';
  showPassword = false;
  showPasswordConfirm = false;
  fieldErrors: Record<string, string[]> = {};
  isLoading: WritableSignal<boolean> = signal(false);
  successMessage: WritableSignal<string | null> = signal<string | null>(null);
  errorMessage: WritableSignal<string | null> = signal<string | null>(null);
  private readonly authService: AuthService = inject(AuthService);
  private readonly router: Router = inject(Router);
  private readonly route: ActivatedRoute = inject(ActivatedRoute);

  /**
   * Initialisiert die Komponente, überprüft die URL-Parameter auf Erfolgsmeldungen und richtet die
   * automatische Fehlerbereinigung ein.
   */
  ngOnInit(): void {
    this.checkQueryParams();

    this.formUtilsShared.setupAutoClearErrors(this.loginForm, (form: FormGroup, field: string): void =>
      this.formUtilsShared.updateFieldErrors(
        form,
        field,
        this.fieldErrors,
        this.formUtilsShared.getErrorMessage.bind(this.formUtilsShared)
      )
    );

    this.formUtilsShared.setupAutoClearErrors(this.registerForm, (form: FormGroup, field: string): void =>
      this.formUtilsShared.updateFieldErrors(
        form,
        field,
        this.fieldErrors,
        this.formUtilsShared.getErrorMessage.bind(this.formUtilsShared)
      )
    );

    this.formUtilsShared.setupAutoClearErrors(this.passwordResetForm, (form: FormGroup, field: string): void =>
      this.formUtilsShared.updateFieldErrors(
        form,
        field,
        this.fieldErrors,
        this.formUtilsShared.getErrorMessage.bind(this.formUtilsShared)
      )
    );
  }

  /**
   * Verarbeitet die Anmeldung des Benutzers.
   * Validiert das Formular, zeigt Lade- und Fehlermeldungen an und navigiert bei Erfolg zum Dashboard.
   */
  onLogin(): void {
    if (this.loginForm.invalid) {
      this.formUtilsShared.markFormGroupTouched(
        this.loginForm,
        this.fieldErrors,
        this.formUtilsShared.getErrorMessage.bind(this.formUtilsShared)
      );

      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.fieldErrors = {};
    const email: string = this.loginForm.controls.email.value;
    const password: string = this.loginForm.controls.password.value;
    this.loginForm.disable();
    this.authService.login(email, password).subscribe({
      next: (): void => {
        this.successMessage.set('Anmeldung erfolgreich!');

        setTimeout((): void => {
          this.registerForm.reset();
          this.fieldErrors = {};
          this.successMessage.set(null);
          this.isLoading.set(false);
          this.router.navigate(['/dashboard']).then();
          this.loginForm.enable();
        }, 4000);
      },
      error: (err: HttpErrorResponse): void => {
        this.handleError(err);
        this.isLoading.set(false);
        this.loginForm.enable();
      },
    });
  }

  /**
   * Verarbeitet die Registrierung eines neuen Benutzers.
   * Validiert das Formular, zeigt Lade- und Fehlermeldungen an und fordert den Benutzer auf, seine E-Mail zu überprüfen.
   */
  onRegister(): void {
    if (this.registerForm.invalid) {
      this.formUtilsShared.markFormGroupTouched(
        this.registerForm,
        this.fieldErrors,
        this.formUtilsShared.getErrorMessage.bind(this.formUtilsShared)
      );

      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.fieldErrors = {};
    const name: string = this.registerForm.controls.name.value;
    const email: string = this.registerForm.controls.email.value;
    const password: string = this.registerForm.controls.password.value;
    const passwordConfirm: string = this.registerForm.controls.passwordConfirm.value;
    this.registerForm.disable();
    this.authService.register(name, email, password, passwordConfirm).subscribe({
      next: (): void => {
        this.successMessage.set(
          'Registrierung erfolgreich! Bitte überprüfen Sie Ihre E-Mail, um Ihr Konto zu verifizieren.'
        );

        setTimeout((): void => {
          this.registerForm.reset();
          this.fieldErrors = {};
          this.successMessage.set(null);
          this.isLoading.set(false);
          this.mode = 'login';
          this.registerForm.enable();
        }, 4000);
      },
      error: (err: HttpErrorResponse): void => {
        this.handleError(err);
        this.isLoading.set(false);
        this.registerForm.enable();
      },
    });
  }

  /**
   * Verarbeitet die Anforderung zum Zurücksetzen des Passworts.
   * Validiert das Formular, zeigt Lade- und Fehlermeldungen an und fordert den Benutzer auf, seine E-Mail zu überprüfen.
   */
  onPasswordReset(): void {
    if (this.passwordResetForm.invalid) {
      this.formUtilsShared.markFormGroupTouched(
        this.passwordResetForm,
        this.fieldErrors,
        this.formUtilsShared.getErrorMessage.bind(this.formUtilsShared)
      );

      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.fieldErrors = {};
    const email: string = this.passwordResetForm.controls.email.value;
    this.passwordResetForm.disable();
    this.authService.requestPasswordReset(email).subscribe({
      next: (): void => {
        this.successMessage.set(
          'Link zum Zurücksetzen des Passworts wurde an Ihre E-Mail gesendet. Bitte überprüfen Sie Ihren Posteingang.'
        );

        setTimeout((): void => {
          this.passwordResetForm.reset();
          this.fieldErrors = {};
          this.successMessage.set(null);
          this.isLoading.set(false);
          this.mode = 'login';
          this.passwordResetForm.enable();
        }, 4000);
      },
      error: (err: HttpErrorResponse): void => {
        this.handleError(err);
        this.isLoading.set(false);
        this.passwordResetForm.enable();
      },
    });
  }

  /**
   * Wechselt die Sichtbarkeit des Passworts im Anmelde- und Registrierungsformular.
   */
  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  /**
   * Wechselt die Sichtbarkeit des Passwortbestätigungsfelds im Registrierungsformular.
   */
  togglePasswordConfirmVisibility(): void {
    this.showPasswordConfirm = !this.showPasswordConfirm;
  }

  /**
   * Überprüft, ob das Anmeldeformular gültig ist.
   * @returns true, wenn das Anmeldeformular gültig ist, andernfalls false.
   */
  isLoginFormValid(): boolean {
    return this.loginForm.valid;
  }

  /**
   * Überprüft, ob das Formular zum Zurücksetzen des Passworts gültig ist.
   *
   * @returns true, wenn das Formular zum Zurücksetzen des Passworts gültig ist, andernfalls false.
   */
  isResetFormVaild(): boolean {
    return this.passwordResetForm.valid;
  }

  /**
   * Überprüft, ob das Registrierungsformular gültig ist.
   *
   * @returns true, wenn das Registrierungsformular gültig ist, andernfalls false.
   */
  isRegisterFormValid(): boolean {
    return this.registerForm.valid;
  }

  /**
   * Wechselt zum Anmeldungsmodus, setzt Fehlermeldungen zurück, entfernt Fehlerzustände,
   * markiert die Formulare als unberührt und setzt die Formulare zurück.
   */
  switchToLogin(): void {
    this.mode = 'login';
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.fieldErrors = {};
    this.registerForm.markAsUntouched();
    this.registerForm.reset();
    this.passwordResetForm.markAsUntouched();
    this.passwordResetForm.reset();
  }

  /**
   * Wechselt zum Registrierungsmodus, setzt Fehlermeldungen zurück, entfernt Fehlerzustände,
   * markiert die Formulare als unberührt und setzt die Formulare zurück.
   */
  switchToRegister(): void {
    this.mode = 'register';
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.fieldErrors = {};
    this.loginForm.markAsUntouched();
    this.loginForm.reset();
    this.passwordResetForm.markAsUntouched();
    this.passwordResetForm.reset();
  }

  /**
   * Wechselt zum Modus für die Passwortzurücksetzung, setzt Fehlermeldungen zurück, entfernt Fehlerzustände,
   * markiert die Formulare als unberührt und setzt die Formulare zurück.
   */
  switchToReset(): void {
    this.mode = 'reset';
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.fieldErrors = {};
    this.registerForm.markAsUntouched();
    this.registerForm.reset();
    this.loginForm.markAsUntouched();
    this.loginForm.reset();
  }

  /**
   * Überprüft die URL-Parameter auf Erfolgsmeldungen für die E-Mail-Verifizierung und die Passwortzurücksetzung.
   * Setzt die entsprechenden Erfolgsmeldungen und wechselt den Modus entsprechend.
   */
  private checkQueryParams(): void {
    this.route.queryParams.subscribe((params: Params): void => {
      if (params['verified'] === 'true') {
        this.successMessage.set('E-Mail erfolgreich verifiziert! Sie können sich jetzt anmelden.');
        this.mode = 'login';
      }

      if (params['reset'] === 'true') {
        this.successMessage.set(
          'Link zum Zurücksetzen des Passworts wurde an Ihre E-Mail gesendet. Klicken Sie auf den Link, um Ihr Passwort zurückzusetzen.'
        );
        this.mode = 'reset';
      }
    });
  }

  /**
   * Gibt einen benutzerdefinierten Validator zurück, der überprüft, ob das Passwort und die Passwortbestätigung
   * übereinstimmen.
   */
  private passwordMatchValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!this.registerForm) {
        return null;
      }

      const password: string | undefined = this.registerForm.get('password')?.value;
      const confirm: string = control.value;

      return password === confirm ? null : { passwordMismatch: true };
    };
  }

  /**
   * Behandelt Fehler, die während der Anmeldung, Registrierung oder Passwortzurücksetzung auftreten können.
   *
   * @param error Der Fehler, der aufgetreten ist, ein HttpErrorResponse von dem AuthService.
   */
  private handleError(error: HttpErrorResponse): void {
    this.fieldErrors = {};
    this.errorMessage.set(error.error.message || 'Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.');

    return;
  }
}
