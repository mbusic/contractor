import { Component, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-servicer-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <h2>Početna stranica</h2>

    <!-- Profile card -->
    <div class="card profile-card">
      <div class="profile-avatar">{{ initials() }}</div>
      <div class="profile-info">
        <div class="profile-name">{{ user()?.displayName }}</div>
        <div class="profile-role">{{ roleLabel() }}</div>
        <div class="profile-branch" *ngIf="user()?.branchId">Poslovnica ID: {{ user()?.branchId }}</div>
      </div>
    </div>

    <!-- Menu tiles -->
    <div class="menu-grid">
      <a routerLink="/orders" class="menu-tile">
        <div class="tile-icon">📋</div>
        <div class="tile-label">Nalozi</div>
        <div class="tile-sub">Aktivni nalozi</div>
      </a>
      <a routerLink="/timesheet" class="menu-tile">
        <div class="tile-icon">📊</div>
        <div class="tile-label">Tablica sati</div>
        <div class="tile-sub">Pregled sati i km</div>
      </a>
      <a routerLink="/orders" [queryParams]="{status: 'RESOLVED'}" class="menu-tile">
        <div class="tile-icon">✅</div>
        <div class="tile-label">Izvršeni nalozi</div>
        <div class="tile-sub">Riješeni nalozi</div>
      </a>
    </div>
  `,
  styles: [`
    .profile-card {
      display: flex;
      align-items: center;
      gap: 20px;
      margin-bottom: 24px;
    }
    .profile-avatar {
      width: 56px; height: 56px;
      border-radius: 50%;
      background: #2e7d32;
      color: white;
      display: flex; align-items: center; justify-content: center;
      font-size: 1.4rem; font-weight: 700;
      flex-shrink: 0;
    }
    .profile-name  { font-size: 1.1rem; font-weight: 600; }
    .profile-role  { font-size: 0.85rem; color: #666; margin-top: 2px; }
    .profile-branch { font-size: 0.8rem; color: #888; margin-top: 2px; }

    .menu-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
      gap: 16px;
    }
    .menu-tile {
      background: white;
      border-radius: 8px;
      box-shadow: 0 1px 4px rgba(0,0,0,0.1);
      padding: 24px 16px;
      text-align: center;
      text-decoration: none;
      color: inherit;
      transition: box-shadow .2s, transform .15s;
      display: block;
      &:hover {
        box-shadow: 0 3px 10px rgba(0,0,0,0.15);
        transform: translateY(-2px);
      }
    }
    .tile-icon  { font-size: 2rem; margin-bottom: 8px; }
    .tile-label { font-weight: 600; font-size: 0.95rem; }
    .tile-sub   { font-size: 0.78rem; color: #888; margin-top: 4px; }
  `],
})
export class ServicerHomeComponent {
  user = computed(() => this.auth.currentUser());

  initials = computed(() => {
    const name = this.auth.currentUser()?.displayName ?? '';
    return name.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);
  });

  roleLabel = computed(() => {
    const map: Record<string, string> = {
      ADMIN: 'Administrator', OFFICE: 'Dispečer',
      SERVICER: 'Serviser', CLIENT: 'Klijent',
    };
    return map[this.auth.currentUser()?.role ?? ''] ?? '';
  });

  constructor(private auth: AuthService) {}
}
