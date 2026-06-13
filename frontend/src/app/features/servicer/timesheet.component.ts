import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrderService } from '../../core/services/order.service';
import { OrderSummary } from '../../core/models/models';

interface TimesheetRow {
  orderNumber: string;
  orderId: number;
  date: string;
  location: string;
  km: number | null;
  totalHours: number | null;
  numberOfWorkers: number | null;
}

@Component({
  selector: 'app-timesheet',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe],
  template: `
    <div class="page-header">
      <h2>Tablica sati</h2>
      <a routerLink="/servicer" class="btn btn-secondary btn-sm">Natrag</a>
    </div>

    <div class="card" style="padding:0;overflow:hidden">
      <table class="table">
        <thead>
          <tr>
            <th>Broj naloga</th>
            <th>Datum</th>
            <th>Lokacija</th>
            <th style="text-align:right">Kilometri</th>
            <th style="text-align:right">Ukupno sati</th>
            <th style="text-align:right">Broj radnika</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let row of rows" [routerLink]="['/orders', row.orderId]" style="cursor:pointer">
            <td>{{ row.orderNumber }}</td>
            <td>{{ row.date | date:'dd.MM.yyyy' }}</td>
            <td>{{ row.location || '-' }}</td>
            <td style="text-align:right">{{ row.km ?? '-' }}</td>
            <td style="text-align:right">{{ row.totalHours ?? '-' }}</td>
            <td style="text-align:right">{{ row.numberOfWorkers ?? '-' }}</td>
          </tr>
          <tr *ngIf="rows.length === 0">
            <td colspan="6" style="text-align:center;color:#888;padding:24px">
              Nema naloga za prikaz.
            </td>
          </tr>
        </tbody>
        <tfoot *ngIf="rows.length > 0">
          <tr class="totals-row">
            <td colspan="3"><strong>Ukupno</strong></td>
            <td style="text-align:right"><strong>{{ totalKm }}</strong></td>
            <td style="text-align:right"><strong>{{ totalHours }}</strong></td>
            <td style="text-align:right"><strong>{{ totalWorkers }}</strong></td>
          </tr>
        </tfoot>
      </table>
    </div>
  `,
  styles: [`
    tfoot .totals-row td {
      border-top: 2px solid #2e7d32;
      background: #e8f5e9;
      padding: 10px 12px;
    }
  `],
})
export class TimesheetComponent implements OnInit {
  rows: TimesheetRow[] = [];

  get totalKm()      { return this.rows.reduce((s, r) => s + (r.km ?? 0), 0); }
  get totalHours()   { return this.rows.reduce((s, r) => s + (r.totalHours ?? 0), 0); }
  get totalWorkers() { return this.rows.reduce((s, r) => s + (r.numberOfWorkers ?? 0), 0); }

  constructor(private orderSvc: OrderService) {}

  ngOnInit() {
    this.orderSvc.list().subscribe(orders => {
      this.rows = orders.map(o => this.toRow(o));
    });
  }

  private toRow(o: OrderSummary): TimesheetRow {
    return {
      orderNumber: o.orderNumber,
      orderId: o.id,
      date: o.createdAt,
      location: o.location ?? '',
      km: o.actualKm,
      totalHours: o.actualTotalHours,
      numberOfWorkers: o.actualNumberOfWorkers,
    };
  }
}
