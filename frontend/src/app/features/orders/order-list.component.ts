import { Component, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrderService } from '../../core/services/order.service';
import { AuthService } from '../../core/services/auth.service';
import { OrderSummary } from '../../core/models/models';

const STATUS_HR: Record<string, string> = {
  DRAFT: 'Nacrt', PENDING: 'Na čekanju',
  IN_PROGRESS: 'U tijeku', RESOLVED: 'Riješen', CANCELLED: 'Otkazan',
};

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page-header">
      <h2>Nalozi</h2>
      <a routerLink="/orders/new" class="btn btn-primary" *ngIf="canCreate()">Dodaj nalog +</a>
    </div>

    <div class="card" style="padding:0;overflow:hidden">
      <table class="table">
        <thead>
          <tr>
            <th>Rb.</th>
            <th>Poslovnica</th>
            <th>Broj naloga</th>
            <th>Hitnost</th>
            <th>Status</th>
            <th>Klijent</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let o of orders; let i = index" style="cursor:pointer" [routerLink]="['/orders', o.id]">
            <td>{{ i + 1 }}</td>
            <td>{{ o.branch?.name ?? '-' }}</td>
            <td>{{ o.orderNumber }}</td>
            <td>{{ o.urgency }}</td>
            <td><span class="status-badge" [class]="o.status">{{ label(o.status) }}</span></td>
            <td>{{ o.clientName ?? '-' }}</td>
          </tr>
          <tr *ngIf="orders.length === 0">
            <td colspan="6" style="text-align:center;color:#888;padding:24px">Nema naloga.</td>
          </tr>
        </tbody>
      </table>
    </div>
  `,
})
export class OrderListComponent implements OnInit {
  orders: OrderSummary[] = [];

  canCreate = computed(() => this.auth.hasRole('OFFICE', 'CLIENT', 'ADMIN'));

  constructor(private orderSvc: OrderService, private auth: AuthService) {}

  ngOnInit() {
    this.orderSvc.list().subscribe(list => this.orders = list);
  }

  label(status: string) { return STATUS_HR[status] ?? status; }
}
