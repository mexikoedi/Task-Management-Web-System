import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { interval, Subscription } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class HeartbeatService {
  private sub?: Subscription;

  constructor(private http: HttpClient) {}

  start() {
    if (this.sub) return;

    this.sub = interval(15000).subscribe(() => {
      this.http.get('http://localhost:8080/api/auth/heartbeat').subscribe({
        next: () => {},
        error: () => {}
      });
    });
  }

  stop() {
    this.sub?.unsubscribe();
    this.sub = undefined;
  }
}
