import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BranchService } from '../../core/services/branch.service';
import { ClientService } from '../../core/services/client.service';
import { UserService } from '../../core/services/user.service';
import { Branch, Client, UserDto } from '../../core/models/models';

type Tab = 'users' | 'branches' | 'clients';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h2>Admin</h2>
    <div class="tab-bar">
      <button [class.active]="tab==='users'"    (click)="switchTab('users')">Korisnici</button>
      <button [class.active]="tab==='branches'" (click)="switchTab('branches')">Poslovnice</button>
      <button [class.active]="tab==='clients'"  (click)="switchTab('clients')">Klijenti</button>
    </div>

    <!-- USERS -->
    <div class="card" *ngIf="tab==='users'">
      <div class="page-header">
        <h3 style="margin:0">Korisnici</h3>
        <button class="btn btn-primary btn-sm" (click)="newUser()">Dodaj +</button>
      </div>
      <form *ngIf="editingUser" class="inline-form" (ngSubmit)="saveUser()">
        <input [(ngModel)]="userForm.username" name="username" placeholder="Korisničko ime" required />
        <input [(ngModel)]="userForm.password" name="password" placeholder="Lozinka" type="password" />
        <select [(ngModel)]="userForm.role" name="role">
          <option value="ADMIN">ADMIN</option>
          <option value="OFFICE">OFFICE</option>
          <option value="SERVICER">SERVICER</option>
          <option value="CLIENT">CLIENT</option>
        </select>
        <input [(ngModel)]="userForm.displayName" name="displayName" placeholder="Ime i prezime" />
        <button type="submit" class="btn btn-primary btn-sm">Spremi</button>
        <button type="button" class="btn btn-secondary btn-sm" (click)="editingUser=false">Odustani</button>
      </form>
      <table class="table" style="margin-top:8px">
        <thead><tr><th>Korisničko ime</th><th>Uloga</th><th>Ime</th><th>Poslovnica</th><th></th></tr></thead>
        <tbody>
          <tr *ngFor="let u of users">
            <td>{{ u.username }}</td><td>{{ u.role }}</td>
            <td>{{ u.displayName }}</td><td>{{ u.branchName ?? '-' }}</td>
            <td><button class="btn btn-danger btn-sm" (click)="deleteUser(u.id)">Izbriši</button></td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- BRANCHES -->
    <div class="card" *ngIf="tab==='branches'">
      <div class="page-header">
        <h3 style="margin:0">Poslovnice</h3>
        <button class="btn btn-primary btn-sm" (click)="newBranch()">Dodaj +</button>
      </div>
      <form *ngIf="editingBranch" class="inline-form" (ngSubmit)="saveBranch()">
        <input [(ngModel)]="branchForm.name" name="name" placeholder="Naziv" required />
        <input [(ngModel)]="branchForm.city" name="city" placeholder="Grad" />
        <button type="submit" class="btn btn-primary btn-sm">Spremi</button>
        <button type="button" class="btn btn-secondary btn-sm" (click)="editingBranch=false">Odustani</button>
      </form>
      <table class="table" style="margin-top:8px">
        <thead><tr><th>Naziv</th><th>Grad</th><th></th></tr></thead>
        <tbody>
          <tr *ngFor="let b of branches">
            <td>{{ b.name }}</td><td>{{ b.city }}</td>
            <td><button class="btn btn-danger btn-sm" (click)="deleteBranch(b.id)">Izbriši</button></td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- CLIENTS -->
    <div class="card" *ngIf="tab==='clients'">
      <div class="page-header">
        <h3 style="margin:0">Klijenti</h3>
        <button class="btn btn-primary btn-sm" (click)="newClient()">Dodaj +</button>
      </div>
      <form *ngIf="editingClient" class="inline-form" (ngSubmit)="saveClient()">
        <select [(ngModel)]="clientForm.type" name="type">
          <option value="COMPANY">Tvrtka</option>
          <option value="INDIVIDUAL">Fizička osoba</option>
        </select>
        <input [(ngModel)]="clientForm.name" name="name" placeholder="Naziv" required />
        <input [(ngModel)]="clientForm.contactPerson" name="contactPerson" placeholder="Kontakt osoba" />
        <input [(ngModel)]="clientForm.phone" name="phone" placeholder="Telefon" />
        <input [(ngModel)]="clientForm.email" name="email" placeholder="E-mail" />
        <button type="submit" class="btn btn-primary btn-sm">Spremi</button>
        <button type="button" class="btn btn-secondary btn-sm" (click)="editingClient=false">Odustani</button>
      </form>
      <table class="table" style="margin-top:8px">
        <thead><tr><th>Naziv</th><th>Tip</th><th>Kontakt</th><th>Telefon</th><th>Lokacije</th><th></th></tr></thead>
        <tbody>
          <ng-container *ngFor="let c of clients">
            <tr>
              <td>{{ c.name }}</td><td>{{ c.type }}</td>
              <td>{{ c.contactPerson }}</td><td>{{ c.phone }}</td>
              <td>
                <button class="btn btn-secondary btn-sm" (click)="toggleLocations(c.id)">
                  Lokacije ({{ c.locations.length }})
                </button>
              </td>
              <td><button class="btn btn-danger btn-sm" (click)="deleteClient(c.id)">Izbriši</button></td>
            </tr>
            <tr *ngIf="expandedClientId === c.id">
              <td colspan="6" class="locations-panel">
                <table class="table locations-table" *ngIf="c.locations.length > 0">
                  <thead><tr><th>Naziv</th><th>Adresa</th><th>Grad</th><th></th></tr></thead>
                  <tbody>
                    <tr *ngFor="let l of c.locations">
                      <td>{{ l.name || '-' }}</td>
                      <td>{{ l.address }}</td>
                      <td>{{ l.city }}</td>
                      <td><button class="btn btn-danger btn-sm" (click)="deleteLocation(c.id, l.id)">Izbriši</button></td>
                    </tr>
                  </tbody>
                </table>
                <p *ngIf="c.locations.length === 0" style="margin:4px 0 8px;color:#666;font-size:.85rem">Nema lokacija.</p>
                <form class="inline-form" (ngSubmit)="saveLocation(c.id)">
                  <input [(ngModel)]="locationForm.name" name="loc-name" placeholder="Naziv (npr. Sjedište)" />
                  <input [(ngModel)]="locationForm.address" name="loc-address" placeholder="Adresa" required />
                  <input [(ngModel)]="locationForm.city" name="loc-city" placeholder="Grad" required />
                  <button type="submit" class="btn btn-primary btn-sm">Dodaj lokaciju</button>
                </form>
              </td>
            </tr>
          </ng-container>
        </tbody>
      </table>
    </div>
  `,
  styles: [`
    .tab-bar { display:flex; gap:4px; margin-bottom:16px;
      button { padding:7px 18px; border:1px solid #ccc; background:white; border-radius:4px 4px 0 0; cursor:pointer;
        &.active { background:#1565c0; color:white; border-color:#1565c0; } } }
    .inline-form { display:flex; flex-wrap:wrap; gap:8px; margin-bottom:12px;
      input, select { padding:6px 10px; border:1px solid #ccc; border-radius:4px; font-size:.875rem; } }
    .locations-panel { background:#f9f9f9; padding:12px 16px; }
    .locations-table { margin-bottom:10px; }
  `],
})
export class AdminComponent implements OnInit {
  tab: Tab = 'users';
  users: UserDto[] = [];
  branches: Branch[] = [];
  clients: Client[] = [];

  editingUser = false;
  editingBranch = false;
  editingClient = false;

  expandedClientId: number | null = null;

  userForm: any = { username: '', password: '', role: 'OFFICE', displayName: '' };
  branchForm: any = { name: '', city: '' };
  clientForm: any = { type: 'COMPANY', name: '', contactPerson: '', phone: '', email: '' };
  locationForm: any = { name: '', address: '', city: '' };

  constructor(
    private userSvc: UserService,
    private branchSvc: BranchService,
    private clientSvc: ClientService,
  ) {}

  ngOnInit() { this.loadAll(); }

  switchTab(t: Tab) { this.tab = t; this.editingUser = this.editingBranch = this.editingClient = false; }

  loadAll() {
    this.userSvc.list().subscribe(u => this.users = u);
    this.branchSvc.list().subscribe(b => this.branches = b);
    this.clientSvc.list().subscribe(c => this.clients = c);
  }

  newUser()    { this.userForm = { username:'', password:'', role:'OFFICE', displayName:'' }; this.editingUser = true; }
  newBranch()  { this.branchForm = { name:'', city:'' }; this.editingBranch = true; }
  newClient()  { this.clientForm = { type:'COMPANY', name:'', contactPerson:'', phone:'', email:'' }; this.editingClient = true; }

  saveUser()   { this.userSvc.create(this.userForm).subscribe(() => { this.editingUser=false; this.loadAll(); }); }
  saveBranch() { this.branchSvc.create(this.branchForm).subscribe(() => { this.editingBranch=false; this.loadAll(); }); }
  saveClient() { this.clientSvc.create(this.clientForm).subscribe(() => { this.editingClient=false; this.loadAll(); }); }

  deleteUser(id: number)   { if (confirm('Izbrisati?')) this.userSvc.delete(id).subscribe(() => this.loadAll()); }
  deleteBranch(id: number) { if (confirm('Izbrisati?')) this.branchSvc.delete(id).subscribe(() => this.loadAll()); }
  deleteClient(id: number) { if (confirm('Izbrisati?')) this.clientSvc.delete(id).subscribe(() => { if (this.expandedClientId === id) this.expandedClientId = null; this.loadAll(); }); }

  toggleLocations(clientId: number) {
    this.expandedClientId = this.expandedClientId === clientId ? null : clientId;
    this.locationForm = { name: '', address: '', city: '' };
  }

  saveLocation(clientId: number) {
    this.clientSvc.addLocation(clientId, this.locationForm).subscribe(updated => {
      this.clients = this.clients.map(c => c.id === clientId ? updated : c);
      this.locationForm = { name: '', address: '', city: '' };
    });
  }

  deleteLocation(clientId: number, locationId: number) {
    if (confirm('Izbrisati lokaciju?')) {
      this.clientSvc.deleteLocation(clientId, locationId).subscribe(() => {
        this.clientSvc.list().subscribe(c => this.clients = c);
      });
    }
  }
}
