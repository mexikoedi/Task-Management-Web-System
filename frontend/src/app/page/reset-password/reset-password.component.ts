import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ValidationErrors,
  ValidatorFn,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { finalize } from 'rxjs/operators';
import { NgIcon } from '@ng-icons/core';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgIcon],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.css',
})
export class ResetPasswordComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);
  readonly router = inject(Router);

  resetForm!: FormGroup;
  isLoading = false;
  showPassword = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  success = false;
  token: string | null = null;

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
      password: ['', [Validators.required, this.strongPasswordValidator()]],
    });
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  getPasswordRequirements(): { text: string; met: boolean }[] {
    const password = this.resetForm.get('password')?.value || '';
    return [
      { text: 'Mindestens 8 Zeichen', met: password.length >= 8 },
      { text: 'Groß- und Kleinbuchstaben', met: /(?=.*[A-Z])(?=.*[a-z])/.test(password) },
      { text: 'Mindestens eine Ziffer (0-9)', met: /\d/.test(password) },
      { text: 'Mindestens ein Sonderzeichen (!@#$%^&*)', met: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?]/.test(password) },
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
    this.authService
      .resetPassword(this.token, password)
      .pipe(
        finalize(() => {
          this.isLoading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
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
        error: err => {
          this.errorMessage = err.error?.message || 'Passwort-Reset fehlgeschlagen.';
          this.resetForm.enable();
          this.cdr.detectChanges();
        },
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
