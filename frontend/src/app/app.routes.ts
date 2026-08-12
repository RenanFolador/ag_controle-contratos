import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard.page').then(m => m.DashboardPage) },
  { path: 'contracts', loadComponent: () => import('./features/contracts/contracts.page').then(m => m.ContractsPage) },
  { path: 'persons', loadComponent: () => import('./features/persons/persons.page').then(m => m.PersonsPage) },
  { path: 'notifications', loadComponent: () => import('./features/notifications/notifications.page').then(m => m.NotificationsPage) },
  { path: 'administration', loadComponent: () => import('./features/administration/administration.page').then(m => m.AdministrationPage) },
  { path: '**', redirectTo: 'dashboard' }
];
