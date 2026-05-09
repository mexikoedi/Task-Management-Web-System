import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import {finalize} from "rxjs/operators";

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="verification-container">
      <div class="verification-card">
        <div *ngIf="isLoading" class="loading">
          <p>Verifiziere Ihre E-Mail-Adresse...</p>
        </div>
        <div *ngIf="success && !isLoading" class="success">
          <h2>✓ E-Mail verifiziert!</h2>
          <p>E-Mail-Adresse erfolgreich verifiziert.</p>
          <p>Sie werden zur Anmeldung weitergeleitet...</p>
        </div>
        <div *ngIf="error && !isLoading" class="error">
          <h2>✗ Verifikation fehlgeschlagen</h2>
          <p>{{ error }}</p>
          <button (click)="router.navigate(['/login'])">Zurück zur Anmeldung</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .verification-container {
      position: fixed;
      inset: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #667eea, #764ba2);
    }
    .verification-card {
      background: white;
      padding: 40px;
      border-radius: 8px;
      text-align: center;
      width: 90%;
      max-width: 400px;
    }
    .loading p {
      font-size: 16px;
      color: #718096;
    }
    .success h2 {
      color: #22863a;
      margin-bottom: 10px;
    }
    .error h2 {
      color: #cb2431;
      margin-bottom: 10px;
    }
    .error button {
      background: #667eea;
      color: white;
      border: none;
      padding: 10px 20px;
      border-radius: 4px;
      cursor: pointer;
      margin-top: 15px;
    }
  `]
})
export class VerifyEmailComponent implements OnInit {
  isLoading = true;
  success = false;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    public router: Router,
    private cdr: ChangeDetectorRef
  ) {}

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
    this.authService.verifyEmail(token).pipe(
      finalize(() => {
        this.isLoading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: () => {
        this.success = true;
        this.cdr.detectChanges();
        setTimeout(() => {
          this.router.navigate(['/login'], { queryParams: { verified: 'true' } });
        }, 4000);
      },
      error: (err) => {
        this.error = err.error?.message || 'E-Mail-Verifikation fehlgeschlagen.';
        this.cdr.detectChanges();
      }
    });
  }
}

