import { Component, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { OrderService } from '../../core/services/order.service';
import { UserService } from '../../core/services/user.service';
import { BranchService } from '../../core/services/branch.service';
import { ClientService } from '../../core/services/client.service';
import { AuthService } from '../../core/services/auth.service';
import { Order, UserDto, Branch, Client } from '../../core/models/models';
import { environment } from '../../../environments/environment';

const STATUS_HR: Record<string, string> = {
  DRAFT: 'Nacrt', PENDING: 'Na čekanju',
  IN_PROGRESS: 'U tijeku', RESOLVED: 'Riješen', CANCELLED: 'Otkazan',
};
const ALL_STATUSES = ['DRAFT', 'PENDING', 'IN_PROGRESS', 'RESOLVED', 'CANCELLED'];

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div *ngIf="order">
      <!-- Header row -->
      <div class="card order-header">
        <div class="header-fields">
          <div><label>Rb.</label><span>{{ order.id }}</span></div>
          <div><label>Poslovnica</label><span>{{ order.branch?.name ?? '-' }}</span></div>
          <div><label>Broj naloga</label><span>{{ order.orderNumber }}</span></div>
          <div><label>Hitnost</label><span>{{ order.urgency }}</span></div>
          <div><label>Status</label>
            <span class="status-badge" [class]="order.status">{{ statusLabel(order.status) }}</span>
          </div>
        </div>
        <div class="header-actions">
          <a routerLink="/orders" class="btn btn-secondary btn-sm">Natrag</a>
        </div>
      </div>

      <!-- Contact info -->
      <div class="card">
        <h3>Informacije o nalogu</h3>
        <ng-container *ngIf="!editing; else editForm">
          <div class="info-grid">
            <div class="info-item"><label>Lokacija</label><p>{{ order.location || '-' }}</p></div>
            <div class="info-item"><label>Kontakt osoba</label><p>{{ order.contactPerson || '-' }}</p></div>
            <div class="info-item"><label>Telefon</label><p>{{ order.phone || '-' }}</p></div>
            <div class="info-item"><label>E-mail</label><p>{{ order.email || '-' }}</p></div>
            <div class="info-item"><label>Klijent</label><p>{{ order.client?.name || '-' }}</p></div>
            <div class="info-item"><label>Serviser</label>
              <p>{{ order.assignedServicer?.displayName || 'Nije dodijeljen' }}</p>
            </div>
          </div>
          <div style="margin-top:12px">
            <label style="font-size:.78rem;color:#888;text-transform:uppercase">Opis naloga</label>
            <p style="margin:4px 0">{{ order.description || '-' }}</p>
          </div>
        </ng-container>

        <ng-template #editForm>
          <div class="info-grid">
            <div class="form-group">
              <label>Poslovnica</label>
              <select [(ngModel)]="editData.branchId">
                <option *ngFor="let b of branches" [ngValue]="b.id">{{ b.name }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>Klijent</label>
              <select [(ngModel)]="editData.clientId">
                <option [ngValue]="null">-- odaberi --</option>
                <option *ngFor="let c of clients" [ngValue]="c.id">{{ c.name }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>Lokacija</label>
              <input [(ngModel)]="editData.location" />
            </div>
            <div class="form-group">
              <label>Kontakt osoba</label>
              <input [(ngModel)]="editData.contactPerson" />
            </div>
            <div class="form-group">
              <label>Telefon</label>
              <input [(ngModel)]="editData.phone" />
            </div>
            <div class="form-group">
              <label>E-mail</label>
              <input [(ngModel)]="editData.email" />
            </div>
            <div class="form-group">
              <label>Hitnost</label>
              <select [(ngModel)]="editData.urgency">
                <option *ngFor="let u of ['Isti dan','1 dan','1 tjedan','1 mjesec','6 mjeseci']" [value]="u">{{ u }}</option>
              </select>
            </div>
            <div class="form-group" *ngIf="isOffice()">
              <label>Serviser</label>
              <select [(ngModel)]="editData.assignedServicerId">
                <option [ngValue]="null">-- nije dodijeljen --</option>
                <option *ngFor="let s of servicers" [ngValue]="s.id">{{ s.displayName }}</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label>Opis naloga</label>
            <textarea [(ngModel)]="editData.description" rows="3"></textarea>
          </div>
        </ng-template>

        <!-- Office action buttons -->
        <div class="action-bar" *ngIf="isOffice()">
          <ng-container *ngIf="!editing">
            <button class="btn btn-primary btn-sm" (click)="startEdit()">Uredi</button>
            <button class="btn btn-danger btn-sm" (click)="deleteOrder()">Izbriši nalog</button>
          </ng-container>
          <ng-container *ngIf="editing">
            <button class="btn btn-primary btn-sm" (click)="saveEdit()">Spremi</button>
            <button class="btn btn-secondary btn-sm" (click)="editing=false">Odustani</button>
          </ng-container>
        </div>

        <!-- Servicer action buttons -->
        <div class="action-bar" *ngIf="isServicer()">
          <button class="btn btn-primary btn-sm"
            *ngIf="order.status === 'PENDING'"
            (click)="changeStatus('IN_PROGRESS')">Prihvati nalog</button>
        </div>
      </div>

      <!-- Status change (office + servicer) -->
      <div class="card" *ngIf="isOffice() || isServicer()">
        <h3>Promjena statusa</h3>
        <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap">
          <select [(ngModel)]="selectedStatus" style="padding:7px 10px;border:1px solid #ccc;border-radius:4px">
            <option *ngFor="let s of allStatuses" [value]="s">{{ statusLabel(s) }}</option>
          </select>
          <button class="btn btn-outline btn-sm" (click)="changeStatus(selectedStatus)">Promijeni status</button>
        </div>
      </div>

      <!-- Cost table -->
      <div class="card">
        <h3>Troškovi</h3>
        <table class="cost-table">
          <thead>
            <tr>
              <th>Stavka</th>
              <th>Procijenjeno</th>
              <th>Stvarno</th>
              <th>Odstupanje</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>Radni sati</td>

              <td>
                <span *ngIf="!editingCost || !isOffice()">{{ order.estimatedWorkHours ?? '-' }}</span>
                <input *ngIf="editingCost && isOffice()" [(ngModel)]="cost.estimatedWorkHours" type="number" style="width:80px" />
              </td>
              <td><span *ngIf="!editingCost">{{ order.actualWorkHours ?? '-' }}</span>
                  <input *ngIf="editingCost" [(ngModel)]="cost.actualWorkHours" type="number" style="width:80px" /></td>
              <td [class]="varianceClass(order.estimatedWorkHours, order.actualWorkHours)">
                {{ variance(order.estimatedWorkHours, order.actualWorkHours) }}</td>
            </tr>
            <tr>
              <td>Broj radnika</td>
              <td>
                <span *ngIf="!editingCost || !isOffice()">{{ order.estimatedNumberOfWorkers ?? '-' }}</span>
                <input *ngIf="editingCost && isOffice()" [(ngModel)]="cost.estimatedNumberOfWorkers" type="number" style="width:80px" />
              </td>
              <td><span *ngIf="!editingCost">{{ order.actualNumberOfWorkers ?? '-' }}</span>
                  <input *ngIf="editingCost" [(ngModel)]="cost.actualNumberOfWorkers" type="number" style="width:80px" /></td>
              <td [class]="varianceClass(order.estimatedNumberOfWorkers, order.actualNumberOfWorkers)">
                {{ variance(order.estimatedNumberOfWorkers, order.actualNumberOfWorkers) }}</td>
            </tr>
            <tr>
              <td>Ukupno sati</td>
              <td>
                <span *ngIf="!editingCost || !isOffice()">{{ order.estimatedTotalHours ?? '-' }}</span>
                <input *ngIf="editingCost && isOffice()" [(ngModel)]="cost.estimatedTotalHours" type="number" style="width:80px" />
              </td>
              <td><span *ngIf="!editingCost">{{ order.actualTotalHours ?? '-' }}</span>
                  <input *ngIf="editingCost" [(ngModel)]="cost.actualTotalHours" type="number" style="width:80px" /></td>
              <td [class]="varianceClass(order.estimatedTotalHours, order.actualTotalHours)">
                {{ variance(order.estimatedTotalHours, order.actualTotalHours) }}</td>
            </tr>
            <tr>
              <td>Kilometri</td>
            <td>
              <span *ngIf="!editingCost || !isOffice()">{{ order.estimatedKm ?? '-' }}</span>
              <input *ngIf="editingCost && isOffice()" [(ngModel)]="cost.estimatedKm" type="number" style="width:80px" />
            </td>
              <td><span *ngIf="!editingCost">{{ order.actualKm ?? '-' }}</span>
                  <input *ngIf="editingCost" [(ngModel)]="cost.actualKm" type="number" style="width:80px" /></td>
              <td [class]="varianceClass(order.estimatedKm, order.actualKm)">
                {{ variance(order.estimatedKm, order.actualKm) }}</td>
            </tr>
            <tr>
              <td>Materijal (EUR)</td>
              <td>
                <span *ngIf="!editingCost || !isOffice()">{{ order.estimatedMaterialCost ?? '-' }}</span>
                <input *ngIf="editingCost && isOffice()" [(ngModel)]="cost.estimatedMaterialCost" type="number" style="width:80px" />
              </td>
              <td><span *ngIf="!editingCost">{{ order.actualMaterialCost ?? '-' }}</span>
                  <input *ngIf="editingCost" [(ngModel)]="cost.actualMaterialCost" type="number" style="width:80px" /></td>
              <td [class]="varianceClass(order.estimatedMaterialCost, order.actualMaterialCost)">
                {{ variance(order.estimatedMaterialCost, order.actualMaterialCost) }}</td>
            </tr>
          </tbody>
        </table>

        <!-- Office can edit estimated too -->
        <div class="action-bar" *ngIf="isOffice()">
          <ng-container *ngIf="!editingCost">
            <button class="btn btn-outline btn-sm" (click)="startCostEdit()">Uredi troškove</button>
          </ng-container>
          <ng-container *ngIf="editingCost">
            <button class="btn btn-primary btn-sm" (click)="saveCost()">Spremi troškove</button>
            <button class="btn btn-secondary btn-sm" (click)="editingCost=false">Odustani</button>
          </ng-container>
        </div>
        <div class="action-bar" *ngIf="isServicer() && !editingCost">
          <button class="btn btn-outline btn-sm" (click)="startCostEdit()">Unesi stvarne troškove</button>
        </div>
        <div class="action-bar" *ngIf="isServicer() && editingCost">
          <button class="btn btn-primary btn-sm" (click)="saveCost()">Spremi</button>
          <button class="btn btn-secondary btn-sm" (click)="editingCost=false">Odustani</button>
        </div>
      </div>

      <!-- Photos -->
      <div class="card">
        <h3>Fotografije</h3>
        <div class="photo-grid">
          <div class="photo-thumb" *ngFor="let p of order.photos">
            <img [src]="fileBaseUrl + p.url" [alt]="'Fotografija'" />
            <button class="photo-delete" *ngIf="canEditPhotos()" (click)="deletePhoto(p.id)">×</button>
          </div>
          <div *ngIf="order.photos.length === 0" style="color:#888">Nema fotografija.</div>
        </div>
        <div class="action-bar" *ngIf="canEditPhotos() && order.photos.length < 6">
          <label class="btn btn-outline btn-sm" style="cursor:pointer">
            Dodaj fotografiju
            <input type="file" accept="image/*" style="display:none" (change)="uploadPhoto($event)" />
          </label>
        </div>
      </div>

      <!-- Notes -->
      <div class="card">
        <h3>Bilješke</h3>
        <div *ngFor="let n of order.notes" class="note-item">
          <span class="note-author">{{ n.authorName }}</span>
          <span class="note-date">{{ n.createdAt | date:'dd.MM.yyyy HH:mm' }}</span>
          <p>{{ n.text }}</p>
        </div>
        <div *ngIf="order.notes.length === 0" style="color:#888;margin-bottom:8px">Nema bilješki.</div>
        <div class="action-bar" *ngIf="isOffice() || isServicer()">
          <textarea [(ngModel)]="newNote" rows="2" placeholder="Nova bilješka..." style="width:100%;margin-bottom:8px;padding:8px;border:1px solid #ccc;border-radius:4px"></textarea>
          <button class="btn btn-outline btn-sm" (click)="addNote()" [disabled]="!newNote.trim()">Bilješke +</button>
        </div>
      </div>

      <!-- Document buttons (office only) -->
      <div class="card" *ngIf="isOffice()">
        <h3>Ispis dokumenata</h3>
        <div style="display:flex;flex-wrap:wrap;gap:8px">
          <button class="btn btn-secondary btn-sm" (click)="openDoc('quote')">Ponuda</button>
          <button class="btn btn-secondary btn-sm" (click)="openDoc('workorder')">Radni nalog</button>
          <button class="btn btn-secondary btn-sm" (click)="openDoc('report')">Izvještaj</button>
          <button class="btn btn-secondary btn-sm" (click)="openDoc('invoice')">Račun</button>
        </div>
      </div>
    </div>

    <div *ngIf="!order && !loading" style="color:#888;padding:24px">Nalog nije pronađen.</div>
  `,
  styles: [`
    .order-header { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; }
    .header-fields { display: flex; flex-wrap: wrap; gap: 16px 32px; }
    .header-fields > div { label { font-size:.75rem; color:#888; display:block; } span { font-weight:600; } }
    .action-bar { margin-top: 16px; display: flex; gap: 8px; flex-wrap: wrap; }
    .note-item { border-left: 3px solid #2e7d32; padding: 6px 12px; margin-bottom: 10px; background: #f9f9f9; border-radius: 0 4px 4px 0; }
    .note-author { font-weight: 600; font-size: .85rem; }
    .note-date { font-size: .78rem; color: #888; margin-left: 8px; }
    .note-item p { margin: 4px 0 0; }
  `],
})
export class OrderDetailComponent implements OnInit {
  fileBaseUrl = environment.fileBaseUrl;
  order: Order | null = null;
  loading = true;
  editing = false;
  editingCost = false;
  editData: any = {};
  cost: any = {};
  newNote = '';
  selectedStatus = '';
  allStatuses = ALL_STATUSES;
  servicers: UserDto[] = [];
  branches: Branch[] = [];
  clients: Client[] = [];

  isOffice = computed(() => this.auth.hasRole('OFFICE', 'ADMIN'));
  isServicer = computed(() => this.auth.hasRole('SERVICER'));
  canEditPhotos = computed(() => this.auth.hasRole('OFFICE', 'ADMIN', 'SERVICER'));

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderSvc: OrderService,
    private userSvc: UserService,
    private branchSvc: BranchService,
    private clientSvc: ClientService,
    private auth: AuthService,
  ) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.orderSvc.get(id).subscribe({
      next: o => { this.order = o; this.selectedStatus = o.status; this.loading = false; },
      error: () => this.loading = false,
    });
    if (this.isOffice()) {
      this.userSvc.list('SERVICER').subscribe(s => this.servicers = s);
      this.branchSvc.list().subscribe(b => this.branches = b);
      this.clientSvc.list().subscribe(c => this.clients = c);
    }
  }

  statusLabel(s: string) { return STATUS_HR[s] ?? s; }

  startEdit() {
    if (!this.order) return;
    this.editData = {
      branchId: this.order.branch?.id ?? null,
      clientId: this.order.client?.id ?? null,
      location: this.order.location,
      contactPerson: this.order.contactPerson,
      phone: this.order.phone,
      email: this.order.email,
      description: this.order.description,
      urgency: this.order.urgency,
      assignedServicerId: this.order.assignedServicer?.id ?? null,
    };
    this.editing = true;
  }

  saveEdit() {
    this.orderSvc.update(this.order!.id, this.editData).subscribe(o => {
      this.order = o; this.editing = false;
    });
  }

  deleteOrder() {
    if (!confirm('Sigurno želite izbrisati nalog?')) return;
    this.orderSvc.delete(this.order!.id).subscribe(() => this.router.navigate(['/orders']));
  }

  changeStatus(status: string) {
    this.orderSvc.changeStatus(this.order!.id, status).subscribe(o => {
      this.order = o; this.selectedStatus = o.status;
    });
  }

  startCostEdit() {
    if (!this.order) return;
    this.cost = {
      actualKm: this.order.actualKm,
      actualWorkHours: this.order.actualWorkHours,
      actualNumberOfWorkers: this.order.actualNumberOfWorkers,
      actualTotalHours: this.order.actualTotalHours,
      actualMaterialCost: this.order.actualMaterialCost,
      estimatedKm: this.order.estimatedKm,
      estimatedWorkHours: this.order.estimatedWorkHours,
      estimatedNumberOfWorkers: this.order.estimatedNumberOfWorkers,
      estimatedTotalHours: this.order.estimatedTotalHours,
      estimatedMaterialCost: this.order.estimatedMaterialCost,
    };
    this.editingCost = true;
  }

  saveCost() {
    this.orderSvc.update(this.order!.id, this.cost).subscribe(o => {
      this.order = o; this.editingCost = false;
    });
  }

  addNote() {
    if (!this.newNote.trim()) return;
    this.orderSvc.addNote(this.order!.id, this.newNote).subscribe(o => {
      this.order = o; this.newNote = '';
    });
  }

  uploadPhoto(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.orderSvc.uploadPhoto(this.order!.id, file).subscribe(o => this.order = o);
  }

  deletePhoto(photoId: number) {
    this.orderSvc.deletePhoto(this.order!.id, photoId).subscribe(() =>
      this.orderSvc.get(this.order!.id).subscribe(o => this.order = o)
    );
  }

  openDoc(type: 'quote' | 'workorder' | 'report' | 'invoice') {
    this.orderSvc.getDocument(this.order!.id, type).subscribe(html => {
      const blob = new Blob([html], { type: 'text/html' });
      const url = URL.createObjectURL(blob);
      const win = window.open(url, '_blank');
      // revoke after the tab has loaded
      if (win) win.addEventListener('load', () => URL.revokeObjectURL(url), { once: true });
    });
  }

  variance(est: number | null, act: number | null): string {
    if (est == null || act == null) return '-';
    const d = act - est;
    return (d > 0 ? '+' : '') + d.toFixed(d % 1 === 0 ? 0 : 1);
  }

  varianceClass(est: number | null, act: number | null): string {
    if (est == null || act == null) return '';
    return act > est ? 'variance-pos' : act < est ? 'variance-neg' : '';
  }
}
