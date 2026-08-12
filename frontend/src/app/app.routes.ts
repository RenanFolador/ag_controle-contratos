import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard.page').then(m => m.DashboardPage) },
  { path: 'contracts/expiring', loadComponent: () => import('./features/contracts/expiring-contracts.page').then(m => m.ExpiringContractsPage) },
  { path: 'contracts/new', loadComponent: () => import('./features/contracts/components/contract-form/contract-form.component').then(m => m.ContractFormComponent) },
  { path: 'contracts/:id/edit', loadComponent: () => import('./features/contracts/components/contract-form/contract-form.component').then(m => m.ContractFormComponent) },
  { path: 'contracts/:id', loadComponent: () => import('./features/contracts/components/contract-detail/contract-detail.component').then(m => m.ContractDetailComponent) },
  { path: 'contracts', loadComponent: () => import('./features/contracts/components/contract-list/contract-list.component').then(m => m.ContractListComponent) },
  { path: 'persons', loadComponent: () => import('./features/persons/persons.page').then(m => m.PersonsPage) },
  { path: 'notifications', loadComponent: () => import('./features/notifications/notifications.page').then(m => m.NotificationsPage) },
  { path: 'administration', loadComponent: () => import('./features/administration/administration.page').then(m => m.AdministrationPage) },
  { path: '**', redirectTo: 'dashboard' }
];
