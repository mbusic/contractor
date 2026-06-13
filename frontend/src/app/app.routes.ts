import { Routes } from '@angular/router';
import { authGuard, adminGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'orders', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login.component').then(m => m.LoginComponent),
  },
  {
    path: 'servicer',
    canActivate: [authGuard],
    loadComponent: () => import('./features/servicer/servicer-home.component').then(m => m.ServicerHomeComponent),
  },
  {
    path: 'timesheet',
    canActivate: [authGuard],
    loadComponent: () => import('./features/servicer/timesheet.component').then(m => m.TimesheetComponent),
  },
  {
    path: 'orders',
    canActivate: [authGuard],
    loadComponent: () => import('./features/orders/order-list.component').then(m => m.OrderListComponent),
  },
  {
    path: 'orders/new',
    canActivate: [authGuard],
    loadComponent: () => import('./features/orders/order-form.component').then(m => m.OrderFormComponent),
  },
  {
    path: 'orders/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./features/orders/order-detail.component').then(m => m.OrderDetailComponent),
  },
  {
    path: 'admin',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./features/admin/admin.component').then(m => m.AdminComponent),
  },
  { path: '**', redirectTo: 'orders' },
];
