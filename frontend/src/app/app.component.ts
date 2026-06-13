import { Component, computed } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from './core/services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, CommonModule],
  template: `
    <nav class="navbar" *ngIf="isLoggedIn()">
      <div class="navbar-brand">Kricco</div>
      <div class="navbar-links">
        <a routerLink="/servicer" *ngIf="isServicer()">Početna</a>
        <a routerLink="/orders">Nalozi</a>
        <a routerLink="/timesheet" *ngIf="isServicer()">Tablica sati</a>
        <a routerLink="/admin" *ngIf="isAdmin()">Admin</a>
      </div>
      <div class="navbar-user">
        <span>{{ userName() }}</span>
        <button class="btn-logout" (click)="logout()">Odjava</button>
      </div>
    </nav>
    <main [class.with-nav]="isLoggedIn()">
      <router-outlet />
    </main>
  `,
  styleUrl: './app.component.scss',
})
export class AppComponent {
  isLoggedIn  = computed(() => !!this.auth.currentUser());
  isAdmin     = computed(() => this.auth.hasRole('ADMIN'));
  isServicer  = computed(() => this.auth.hasRole('SERVICER'));
  userName    = computed(() => this.auth.currentUser()?.displayName ?? '');

  constructor(private auth: AuthService, private router: Router) {}

  logout() { this.auth.logout(); }
}
