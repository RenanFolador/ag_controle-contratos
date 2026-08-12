import { inject, Injectable } from '@angular/core';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class PermissionService {
  private readonly auth = inject(AuthService);

  canManageContracts(): boolean {
    return this.auth.hasAnyRole('ADMIN', 'CONTRACT_MANAGER');
  }

  canManageAssignments(): boolean {
    return this.canManageContracts();
  }

  canManagePersons(): boolean {
    return this.auth.hasAnyRole('ADMIN');
  }

  canViewPersons(): boolean {
    return this.auth.hasAnyRole('ADMIN', 'CONTRACT_MANAGER', 'VIEWER');
  }

  canViewNotifications(): boolean {
    return this.auth.hasAnyRole('ADMIN', 'CONTRACT_MANAGER', 'VIEWER');
  }

  canViewDashboard(): boolean {
    return this.auth.hasAnyRole('ADMIN', 'CONTRACT_MANAGER', 'VIEWER');
  }

  canViewReports(): boolean {
    return this.auth.hasAnyRole('ADMIN', 'CONTRACT_MANAGER', 'VIEWER');
  }

  canManageAdministration(): boolean {
    return this.auth.hasAnyRole('ADMIN');
  }
}
