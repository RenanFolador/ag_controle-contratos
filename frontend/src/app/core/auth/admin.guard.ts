import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { PermissionService } from './permission.service';

export const adminGuard: CanActivateFn = () => {
  const permissions = inject(PermissionService);
  const router = inject(Router);
  return permissions.canManageAdministration() ? true : router.parseUrl('/dashboard');
};
