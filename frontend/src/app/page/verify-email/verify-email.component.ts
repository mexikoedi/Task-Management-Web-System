import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { finalize } from 'rxjs/operators';
import { NgIcon } from '@ng-icons/core';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, NgIcon],
  templateUrl: './verify-email.component.html',
  styleUrl: './verify-email.component.css',
})
export class VerifyEmailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);
  readonly router = inject(Router);

  isLoading = true;
  success = false;
  error: string | null = null;

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const token = params['token'];
      if (!token) {
        this.isLoading = false;
        this.error = 'Verifikationstoken fehlt.';
        return;
      }
      this.verifyEmail(token);
    });
  }

  private verifyEmail(token: string): void {
    this.authService
      .verifyEmail(token)
      .pipe(
        finalize(() => {
          this.isLoading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.success = true;
          this.cdr.detectChanges();
          setTimeout(() => {
            this.router.navigate(['/login'], {
              queryParams: { verified: 'true' },
            });
          }, 4000);
        },
        error: err => {
          this.error = err.error?.message || 'E-Mail-Verifikation fehlgeschlagen.';
          this.cdr.detectChanges();
        },
      });
  }
}
