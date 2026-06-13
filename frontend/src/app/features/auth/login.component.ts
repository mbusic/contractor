import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule],
  template: `
    <div class="login-wrap">
      <div class="login-box">
        <div class="login-logo">Kricco</div>
        <h2>Prijava</h2>
        <div class="form-group">
          <label>Korisničko ime</label>
          <input [(ngModel)]="username" type="text" autocomplete="username" (keyup.enter)="login()" />
        </div>
        <div class="form-group">
          <label>Lozinka</label>
          <input [(ngModel)]="password" type="password" autocomplete="current-password" (keyup.enter)="login()" />
        </div>
        <div class="error" *ngIf="error">{{ error }}</div>
        <button class="btn btn-primary" style="width:100%" (click)="login()" [disabled]="loading">
          {{ loading ? 'Prijava...' : 'Prijava' }}
        </button>
      </div>
    </div>
  `,
  styles: [`
    .login-wrap {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f5f5;
    }
    .login-box {
      background: white;
      padding: 36px 32px;
      border-radius: 8px;
      box-shadow: 0 2px 12px rgba(0,0,0,0.15);
      width: 100%;
      max-width: 360px;
    }
    .login-logo {
      font-size: 1.6rem;
      font-weight: 700;
      color: #2e7d32;
      text-align: center;
      margin-bottom: 8px;
    }
    h2 { text-align: center; color: #555; margin-bottom: 24px; }
    .error { color: #c62828; font-size: 0.85rem; margin-bottom: 10px; }
  `],
})
export class LoginComponent {
  username = '';
  password = '';
  error = '';
  loading = false;

  constructor(private auth: AuthService, private router: Router) {}

  login() {
    if (!this.username || !this.password) return;
    this.loading = true;
    this.error = '';
    this.auth.login(this.username, this.password).subscribe({
      next: () => this.router.navigate(['/orders']),
      error: () => {
        this.error = 'Pogrešno korisničko ime ili lozinka.';
        this.loading = false;
      },
    });
  }
}
