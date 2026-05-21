import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { trigger, transition, style, animate } from '@angular/animations';
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { ChangeDetectorRef } from '@angular/core';
import { NgIcon } from '@ng-icons/core';
import { HeartbeatService } from '../../service/heartbeat.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgIcon],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  animations: [
    trigger('slideIn', [
      transition(':enter', [
        style({ transform: 'translateY(-50px)', opacity: 0 }),
        animate('300ms ease-out', style({ transform: 'translateY(0)', opacity: 1 })),
      ]),
    ]),
  ],
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  registerForm!: FormGroup;
  resetForm!: FormGroup;
  mode: 'login' | 'register' | 'reset' = 'login';
  isLoading = false;
  showPassword = false;
  showPasswordConfirm = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  fieldErrors: { [key: string]: string[] } = {};

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
    private heartbeat: HeartbeatService
  ) {}

  ngOnInit(): void {
    this.initializeForms();
    this.checkQueryParams();
  }

  /**
   * Initialisiere login und register Forms
   */
  private initializeForms(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.maxLength(30), this.emailValidator()]],
      password: ['', [Validators.required]],
    });

    this.registerForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(30)]],
      email: ['', [Validators.required, Validators.maxLength(30), this.emailValidator()]],
      password: ['', [Validators.required, this.passwordStrengthValidator()]],
      passwordConfirm: ['', [Validators.required, this.passwordMatchValidator()]],
    });

    this.resetForm = this.fb.group({
      email: ['', [Validators.required, Validators.maxLength(30), this.emailValidator()]],
    });
  }

  /**
   * Prüfe Query-Parameter für Email-Verifikation oder Reset
   */
  private checkQueryParams(): void {
    this.route.queryParams.subscribe(params => {
      if (params['verified'] === 'true') {
        this.successMessage = 'E-Mail erfolgreich verifiziert! Sie können sich jetzt anmelden.';
        this.mode = 'login';
      }
      if (params['reset'] === 'true') {
        this.successMessage =
          'Link zum Zurücksetzen des Passworts wurde an Ihre E-Mail gesendet. Klicken Sie auf den Link, um Ihr Passwort zurückzusetzen.';
        this.mode = 'reset';
      }
    });
  }

  /**
   * Anmeldung
   */
  onLogin(): void {
    if (this.loginForm.invalid) {
      this.markFormGroupTouched(this.loginForm);
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;
    this.fieldErrors = {};

    const { email, password } = this.loginForm.value;
    this.loginForm.disable();
    this.authService
      .login(email, password)
      .pipe(
        finalize(() => {
          this.isLoading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.successMessage = 'Anmeldung erfolgreich!';
          this.cdr.detectChanges();
          setTimeout(() => {
            this.registerForm.reset();
            this.fieldErrors = {};
            this.successMessage = null;
            this.heartbeat.start();
            this.router.navigate(['/dashboard']);
            this.loginForm.enable();
          }, 4000);
        },
        error: err => {
          this.handleError(err);
          this.loginForm.enable();
          this.cdr.detectChanges();
        },
      });
  }

  /**
   * Registrierung
   */
  onRegister(): void {
    if (this.registerForm.invalid) {
      this.markFormGroupTouched(this.registerForm);
      this.errorMessage = 'Bitte füllen Sie alle erforderlichen Felder aus.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;
    this.fieldErrors = {};

    const { name, email, password, passwordConfirm } = this.registerForm.value;
    this.registerForm.disable();
    this.authService
      .register(name, email, password, passwordConfirm)
      .pipe(
        finalize(() => {
          this.isLoading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.successMessage =
            'Registrierung erfolgreich! Bitte überprüfen Sie Ihre E-Mail, um Ihr Konto zu verifizieren.';
          this.cdr.detectChanges();
          setTimeout(() => {
            this.mode = 'login';
            this.registerForm.reset();
            this.fieldErrors = {};
            this.successMessage = null;
            this.registerForm.enable();
          }, 4000);
        },
        error: err => {
          this.handleError(err);
          this.registerForm.enable();
          this.cdr.detectChanges();
        },
      });
  }

  /**
   * Passwort-Reset anfordern
   */
  onPasswordReset(): void {
    if (this.resetForm.invalid) {
      this.markFormGroupTouched(this.resetForm);
      this.errorMessage = 'Bitte geben Sie eine gültige E-Mail-Adresse ein.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;
    this.fieldErrors = {};

    const email = this.resetForm.get('email')?.value as string;
    this.resetForm.disable();
    this.authService
      .requestPasswordReset(email)
      .pipe(
        finalize(() => {
          this.isLoading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.successMessage =
            'Link zum Zurücksetzen des Passworts wurde an Ihre E-Mail gesendet. Bitte überprüfen Sie Ihren Posteingang.';
          this.cdr.detectChanges();
          setTimeout(() => {
            this.mode = 'login';
            this.resetForm.reset();
            this.fieldErrors = {};
            this.successMessage = null;
            this.resetForm.enable();
          }, 4000);
        },
        error: err => {
          this.handleError(err);
          this.resetForm.enable();
          this.cdr.detectChanges();
        },
      });
  }

  /**
   * Toggle Passwort-Sichtbarkeit
   */
  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  togglePasswordConfirmVisibility(): void {
    this.showPasswordConfirm = !this.showPasswordConfirm;
  }

  /**
   * Validiere Passwort-Stärke
   */
  private passwordStrengthValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const password = control.value || '';

      const strong = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?]).{8,}$/.test(password);

      return strong ? null : { weakPassword: true };
    };
  }

  /**
   * Validiere E-Mail-Format
   */
  private emailValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const email = control.value || '';

      const pattern = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

      return pattern.test(email) ? null : { invalidEmail: true };
    };
  }

  /**
   * Validiere Passwort-Übereinstimmung
   */
  private passwordMatchValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!this.registerForm) {
        return null;
      }

      const password = this.registerForm.get('password')?.value;
      const confirm = control.value;

      return password === confirm ? null : { passwordMismatch: true };
    };
  }

  /**
   * Prüfe einzelne Passwort-Anforderungen
   */
  getPasswordRequirements(): Array<{ text: string; met: boolean }> {
    const password = this.registerForm.get('password')?.value || '';
    return [
      { text: 'Mindestens 8 Zeichen', met: password.length >= 8 },
      { text: 'Groß- und Kleinbuchstaben', met: /(?=.*[A-Z])(?=.*[a-z])/.test(password) },
      { text: 'Mindestens eine Ziffer (0-9)', met: /\d/.test(password) },
      { text: 'Mindestens ein Sonderzeichen (!@#$%^&*)', met: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?]/.test(password) },
    ];
  }

  /**
   * Aktualisiere Fehler für ein bestimmtes Feld basierend auf Validierungsstatus
   */
  updateFieldErrors(formGroup: FormGroup, fieldName: string): void {
    const control = formGroup.get(fieldName);

    if (!control) {
      return;
    }

    if (control.errors && control.touched) {
      this.fieldErrors[fieldName] = Object.keys(control.errors).map(errorKey =>
        this.getErrorMessage(fieldName, errorKey, control.errors?.[errorKey])
      );
    } else {
      delete this.fieldErrors[fieldName];
    }
  }

  /**
   * Fehlerbehandlung
   */
  private handleError(error: any): void {
    this.isLoading = false;
    this.fieldErrors = {};

    if (error.status === 401) {
      this.errorMessage = 'Ungültige E-Mail oder Passwort.';

      return;
    }

    if (error.status == 409) {
      this.errorMessage =
        'E-Mail ist bereits registriert. Bitte melden Sie sich an oder verwenden Sie eine andere E-Mail.';

      return;
    }

    if (error.status == 404) {
      this.errorMessage = 'Benutzer nicht gefunden. Bitte überprüfen Sie Ihre E-Mail oder registrieren Sie sich.';

      return;
    }

    if (error.status == 403) {
      this.errorMessage = 'Benutzer deaktiviert. Bitte überprüfen Sie Ihre E-Mail oder kontaktieren Sie uns.';

      return;
    }

    if (error.status === 400 && error.error?.errors) {
      this.fieldErrors = error.error.errors;
      this.errorMessage = 'Bitte korrigieren Sie die Eingaben.';

      return;
    }

    this.errorMessage = error.error?.message || 'Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.';
  }

  /**
   * Markiere alle FormControl als touched für Validierungsanzeige
   */
  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();

      if (control && control.errors) {
        this.fieldErrors[key] = Object.keys(control.errors).map(errorKey =>
          this.getErrorMessage(key, errorKey, control.errors?.[errorKey])
        );
      }
    });
  }

  /**
   * Generiere Fehlermeldungen
   */
  private getErrorMessage(fieldName: string, errorType: string, errorValue: any): string {
    const messages: { [key: string]: string } = {
      required: `${this.getFieldDisplayName(fieldName)} ist erforderlich`,
      invalidEmail: 'Bitte geben Sie eine gültige E-Mail-Adresse ein',
      minlength: `${this.getFieldDisplayName(fieldName)} muss mindestens ${errorValue?.requiredLength} Zeichen lang sein`,
      maxlength: `${this.getFieldDisplayName(fieldName)} darf nicht mehr als ${errorValue?.requiredLength} Zeichen lang sein`,
      weakPassword: 'Passwort erfüllt nicht alle Anforderungen',
      passwordMismatch: 'Passwörter stimmen nicht überein',
    };
    return messages[errorType] || `${this.getFieldDisplayName(fieldName)} ist ungültig`;
  }

  /**
   * Mappe Feldnamen zu benutzerfreundlichen Anzeigenamen
   */
  private getFieldDisplayName(fieldName: string): string {
    const fieldNames: { [key: string]: string } = {
      name: 'Name',
      email: 'E-Mail-Adresse',
      password: 'Passwort',
      passwordConfirm: 'Passwortbestätigung',
    };

    return fieldNames[fieldName] || this.capitalize(fieldName);
  }

  /**
   * Kapitalisiere String
   */
  private capitalize(str: string): string {
    return str.charAt(0).toUpperCase() + str.slice(1);
  }

  /**
   * Getter für Form-Validierung
   */
  get isLoginFormValid(): boolean {
    return this.loginForm.valid;
  }

  get isRegisterFormValid(): boolean {
    return this.registerForm.valid;
  }

  /**
   * Switch zwischen Login/Register/Reset Modi
   */
  switchToRegister(): void {
    this.mode = 'register';
    this.errorMessage = null;
    this.successMessage = null;
    this.fieldErrors = {};
    this.loginForm.markAsUntouched();
    this.resetForm.markAsUntouched();
  }

  switchToLogin(): void {
    this.mode = 'login';
    this.errorMessage = null;
    this.successMessage = null;
    this.fieldErrors = {};
    this.registerForm.markAsUntouched();
    this.resetForm.markAsUntouched();
  }

  switchToReset(): void {
    this.mode = 'reset';
    this.errorMessage = null;
    this.successMessage = null;
    this.fieldErrors = {};
    this.registerForm.markAsUntouched();
    this.loginForm.markAsUntouched();
  }
}
