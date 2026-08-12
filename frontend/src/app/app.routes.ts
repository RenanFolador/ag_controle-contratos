import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: '',
    canActivateChild: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/components/dashboard/dashboard.component').then(
            (m) => m.DashboardComponent,
          ),
      },
      {
        path: 'contracts/expiring',
        loadComponent: () =>
          import('./features/contracts/expiring-contracts.page').then(
            (m) => m.ExpiringContractsPage,
          ),
      },
      {
        path: 'contracts/new',
        loadComponent: () =>
          import('./features/contracts/components/contract-form/contract-form.component').then(
            (m) => m.ContractFormComponent,
          ),
      },
      {
        path: 'contracts/:id/edit',
        loadComponent: () =>
          import('./features/contracts/components/contract-form/contract-form.component').then(
            (m) => m.ContractFormComponent,
          ),
      },
      {
        path: 'contracts/:id',
        loadComponent: () =>
          import('./features/contracts/components/contract-detail/contract-detail.component').then(
            (m) => m.ContractDetailComponent,
          ),
      },
      {
        path: 'contracts',
        loadComponent: () =>
          import('./features/contracts/components/contract-list/contract-list.component').then(
            (m) => m.ContractListComponent,
          ),
      },
      {
        path: 'persons/new',
        loadComponent: () =>
          import('./features/persons/components/person-form/person-form.component').then(
            (m) => m.PersonFormComponent,
          ),
      },
      {
        path: 'persons/:id/edit',
        loadComponent: () =>
          import('./features/persons/components/person-form/person-form.component').then(
            (m) => m.PersonFormComponent,
          ),
      },
      {
        path: 'persons/:id',
        loadComponent: () =>
          import('./features/persons/components/person-detail/person-detail.component').then(
            (m) => m.PersonDetailComponent,
          ),
      },
      {
        path: 'persons',
        loadComponent: () =>
          import('./features/persons/components/person-list/person-list.component').then(
            (m) => m.PersonListComponent,
          ),
      },
      {
        path: 'notificacoes',
        loadComponent: () =>
          import('./features/notifications/components/notification-list/notification-list.component').then(
            (m) => m.NotificationListComponent,
          ),
      },
      { path: 'notifications', redirectTo: 'notificacoes' },
      {
        path: 'administration',
        loadComponent: () =>
          import('./features/administration/administration.page').then((m) => m.AdministrationPage),
      },
      { path: '**', redirectTo: 'dashboard' },
    ],
  },
];
