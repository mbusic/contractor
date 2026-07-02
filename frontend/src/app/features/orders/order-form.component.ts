import { Component, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { OrderService } from '../../core/services/order.service';
import { BranchService } from '../../core/services/branch.service';
import { ClientService } from '../../core/services/client.service';
import { AuthService } from '../../core/services/auth.service';
import { Branch, Client } from '../../core/models/models';

const URGENCY_OPTIONS = ['Isti dan', '1 dan', '1 tjedan', '1 mjesec', '6 mjeseci'];

@Component({
  selector: 'app-order-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-header">
      <h2>Novi nalog</h2>
      <button class="btn btn-secondary" (click)="back()">Odustani</button>
    </div>

    <div class="card">
      <div class="info-grid">
        <div class="form-group" *ngIf="isOffice()">
          <label>Poslovnica</label>
          <select [(ngModel)]="form.branchId">
            <option [ngValue]="null">-- odaberi --</option>
            <option *ngFor="let b of branches" [ngValue]="b.id">{{ b.name }}</option>
          </select>
        </div>

        <div class="form-group" *ngIf="isOffice()">
          <label>Klijent</label>
          <select [(ngModel)]="form.clientId" (change)="onClientChange()">
            <option [ngValue]="null">-- odaberi --</option>
            <option *ngFor="let c of clients" [ngValue]="c.id">{{ c.name }}</option>
          </select>
        </div>

        <div class="form-group">
          <label>Lokacija</label>
          <select *ngIf="clientLocations.length > 0; else locationInput" [(ngModel)]="form.location">
            <option value="">-- odaberi lokaciju --</option>
            <option *ngFor="let l of clientLocations" [value]="l.address + ', ' + l.city">
              {{ l.name ? l.name + ' – ' : '' }}{{ l.address }}, {{ l.city }}
            </option>
          </select>
          <ng-template #locationInput>
            <input [(ngModel)]="form.location" type="text" placeholder="Ulica i grad" />
          </ng-template>
        </div>

        <div class="form-group">
          <label>Kontakt osoba</label>
          <input [(ngModel)]="form.contactPerson" type="text" />
        </div>

        <div class="form-group">
          <label>Telefon</label>
          <input [(ngModel)]="form.phone" type="text" />
        </div>

        <div class="form-group">
          <label>E-mail</label>
          <input [(ngModel)]="form.email" type="email" />
        </div>

        <div class="form-group">
          <label>Hitnost</label>
          <select [(ngModel)]="form.urgency">
            <option value="">-- odaberi --</option>
            <option *ngFor="let u of urgencyOptions" [value]="u">{{ u }}</option>
          </select>
        </div>
      </div>

      <div class="form-group">
        <label>Opis naloga</label>
        <textarea [(ngModel)]="form.description" rows="4"></textarea>
      </div>

      <div class="error" *ngIf="error">{{ error }}</div>

      <button class="btn btn-primary" (click)="submit()" [disabled]="saving">
        {{ saving ? 'Kreiranje...' : 'Kreiraj' }}
      </button>
    </div>
  `,
  styles: [`.error { color: #c62828; margin-bottom: 10px; font-size: 0.85rem; }`],
})
export class OrderFormComponent implements OnInit {
  branches: Branch[] = [];
  clients: Client[] = [];
  clientLocations: { address: string; city: string }[] = [];
  urgencyOptions = URGENCY_OPTIONS;
  saving = false;
  error = '';

  form: any = {
    branchId: null, clientId: null, location: '',
    contactPerson: '', phone: '', email: '',
    description: '', urgency: '',
  };

  isOffice = computed(() => this.auth.hasRole('OFFICE', 'ADMIN'));

  constructor(
    private orderSvc: OrderService,
    private branchSvc: BranchService,
    private clientSvc: ClientService,
    private auth: AuthService,
    private router: Router,
  ) {}

  ngOnInit() {
    if (this.isOffice()) {
      this.branchSvc.list().subscribe(b => this.branches = b);
      this.clientSvc.list().subscribe(c => this.clients = c);
    }
  }

  onClientChange() {
    const client = this.clients.find(c => c.id === this.form.clientId);
    this.clientLocations = client?.locations ?? [];
    if (client) {
      this.form.contactPerson = client.contactPerson ?? '';
      this.form.phone = client.phone ?? '';
      this.form.email = client.email ?? '';
    }
    this.form.location = '';
  }

  submit() {
    if (!this.form.description) { this.error = 'Opis naloga je obavezan.'; return; }
    this.saving = true;
    this.error = '';
    this.orderSvc.create(this.form).subscribe({
      next: order => this.router.navigate(['/orders', order.id]),
      error: () => { this.error = 'Greška pri kreiranju naloga.'; this.saving = false; },
    });
  }

  back() { this.router.navigate(['/orders']); }
}
