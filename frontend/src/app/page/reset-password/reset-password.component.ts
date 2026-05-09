import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
  ValidationErrors,
  ValidatorFn,
  AbstractControl
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import {finalize} from "rxjs/operators";

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="reset-container">
      <div class="reset-card">
        <div class="login-header">
            <h1>Passwort zurücksetzen</h1>
            <p class="subtitle">Wählen Sie ein starkes neues Passwort</p>
        </div>

        <div *ngIf="errorMessage" class="error-message">
          <span class="icon">✕</span>
          {{ errorMessage }}
        </div>

        <div *ngIf="successMessage" class="success-message">
          <span class="icon">✓</span>
          {{ successMessage }}
        </div>

        <form *ngIf="!success" [formGroup]="resetForm" (ngSubmit)="onResetPassword()" class="form">
          <div class="form-group">
            <label for="password" class="form-label">
              Neues Passwort
              <span class="tooltip" title="Passwort muss mindestens 8 Zeichen enthalten, Groß- und Kleinbuchstaben, Ziffer und Sonderzeichen">
                ℹ️
              </span>
            </label>
            <input
              id="password"
              [type]="showPassword ? 'text' : 'password'"
              formControlName="password"
              placeholder="Geben Sie ein neues Passwort ein"
              class="form-input"
              [class.error]="resetForm.get('password')?.invalid && resetForm.get('password')?.touched"
              (blur)="resetForm.get('password')?.markAsTouched()"
            />
            <div *ngIf="resetForm.get('password')?.touched && resetForm.get('password')?.invalid" class="error-text">
              <div *ngIf="resetForm.get('password')?.errors?.['required']">
                Passwort ist erforderlich
              </div>
              <div *ngIf="resetForm.get('password')?.errors?.['weakPassword']">
                Passwort erfüllt nicht alle Anforderungen
              </div>
            </div>
            <button
              type="button"
              class="password-toggle"
              (click)="togglePasswordVisibility()"
              tabindex="-1"
            >
              {{ showPassword ? '👁️‍🗨️' : '👁️' }}
            </button>
            <div class="password-requirements">
              <small *ngFor="let req of getPasswordRequirements()" [class.met]="req.met">
                {{ req.met ? '✓' : '✗' }} {{ req.text }}
              </small>
            </div>
          </div>

          <button type="submit" class="submit-btn" [disabled]="!resetForm.valid || isLoading">
            {{ isLoading ? 'Wird zurückgesetzt...' : 'Passwort zurücksetzen' }}
          </button>
        </form>

        <div class="form-links">
          <button type="button" class="link-button" (click)="router.navigate(['/login'])">
            Zurück zur Anmeldung
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .reset-container {
      position: fixed;
      inset: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #667eea, #764ba2);
    }
    .reset-card {
      background: white;
      padding: 40px;
      border-radius: 12px;
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
      width: 90%;
      max-width: 420px;
    }
    h1 {
      text-align: center;
      color: #2d3748;
      margin-bottom: 20px;
    }
    .login-header {
      text-align: center;
      margin-bottom: 20px;
    }
    .login-header h1 {
      margin: 0 0 8px;
      color: #2d3748;
      font-size: 28px;
    }
    .login-header .subtitle {
      margin: 0;
      color: #718096;
      font-size: 14px;
    }
    .error-message, .success-message {
      display: flex;
      gap: 10px;
      align-items: center;
      margin-bottom: 20px;
      padding: 12px 14px;
      border-radius: 8px;
      font-size: 14px;
    }
    .error-message {
      background: #fed7d7;
      color: #742a2a;
    }
    .error-text{
      font-size: 12px;
      color: #f56565;
    }
    .success-message {
      background: #c6f6d5;
      color: #22543d;
    }
    .form {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }
    .form-group {
      display: flex;
      flex-direction: column;
      gap: 6px;
      position: relative;
    }
    .form-label {
      font-size: 14px;
      font-weight: 600;
      color: #2d3748;
    }
    .form-input {
      padding: 12px 14px;
      border: 2px solid #e2e8f0;
      border-radius: 6px;
      background: #f7fafc;
      font: inherit;
      font-size: 14px;
      transition: 0.2s;
    }
    .form-input:focus {
      outline: 0;
      border-color: #667eea;
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
    }
    .form-input.error {
      border-color: #f56565;
      background: #fff5f5;
    }
    .password-toggle {
      position: absolute;
      right: 12px;
      top: 32px;
      border: 0;
      background: 0 0;
      cursor: pointer;
      opacity: 0.7;
      font-size: 16px;
    }
    .password-toggle:hover {
      opacity: 1;
    }
    .password-requirements {
      font-size: 12px;
      color: #718096;
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
    .password-requirements small {
      display: block;
    }
    .password-requirements small.met {
      color: #22863a;
      font-weight: 600;
    }
    .submit-btn {
      padding: 12px;
      border: 0;
      border-radius: 6px;
      background: linear-gradient(135deg, #667eea, #764ba2);
      color: white;
      font-size: 15px;
      font-weight: 600;
      cursor: pointer;
      transition: 0.2s;
    }
    .submit-btn:hover:not(:disabled) {
      transform: translateY(-1px);
    }
    .submit-btn:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
    .form-links {
      display: flex;
      justify-content: center;
      margin-top: 16px;
    }
    .tooltip {
      cursor: help;
      font-size: 12px;
      opacity: 0.7;
    }
    .link-button {
      border: 0;
      background: 0 0;
      color: #667eea;
      cursor: pointer;
      font-weight: 600;
      text-decoration: underline;
    }
  `]
})
export class ResetPasswordComponent implements OnInit {
  resetForm!: FormGroup;
  isLoading = false;
  showPassword = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  success = false;
  token: string | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private authService: AuthService,
    public router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.route.queryParams.subscribe(params => {
      this.token = params['token'];
      if (!this.token) {
        this.errorMessage = 'Passwort-Reset-Token fehlt.';
      }
    });
  }

  private initializeForm(): void {
    this.resetForm = this.fb.group({
      password: [
        '',
        [Validators.required, this.strongPasswordValidator()]
      ]
    });
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  getPasswordRequirements(): Array<{ text: string; met: boolean }> {
    const password = this.resetForm.get('password')?.value || '';
    return [
      { text: 'Mindestens 8 Zeichen', met: password.length >= 8 },
      { text: 'Groß- und Kleinbuchstaben', met: /(?=.*[A-Z])(?=.*[a-z])/.test(password) },
      { text: 'Mindestens eine Ziffer (0-9)', met: /\d/.test(password) },
      { text: 'Mindestens ein Sonderzeichen (!@#$%^&*)', met: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?]/.test(password) }
    ];
  }

  onResetPassword(): void {
    if (!this.token || this.resetForm.invalid) {
      this.errorMessage = 'Bitte füllen Sie das Formular korrekt aus.';
      return;
    }

    const password = this.resetForm.get('password')?.value;
    if (!this.isPasswordStrong(password)) {
      this.errorMessage = 'Passwort erfüllt nicht alle Anforderungen.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;

    this.resetForm.disable();
    this.authService.resetPassword(this.token, password).pipe(
      finalize(() => {
        this.isLoading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: () => {
        this.success = true;
        this.successMessage = 'Passwort erfolgreich zurückgesetzt! Sie werden zur Anmeldung weitergeleitet...';
        this.cdr.detectChanges();
        setTimeout(() => {
          this.resetForm.reset();
          this.successMessage = null;
          this.router.navigate(['/login']);
          this.resetForm.enable();
        }, 4000);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Passwort-Reset fehlgeschlagen.';
        this.resetForm.enable();
        this.cdr.detectChanges();
      }
    });
  }

  private isPasswordStrong(password: string): boolean {
    const pattern = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?]).{8,}$/;
    return pattern.test(password);
  }

  private strongPasswordValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const password = control.value || '';

      const valid =
        password.length >= 8 &&
        /[A-Z]/.test(password) &&
        /[a-z]/.test(password) &&
        /\d/.test(password) &&
        /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?]/.test(password);

      return valid ? null : { weakPassword: true };
    };
  }
}

