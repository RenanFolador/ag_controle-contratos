import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';
import { PermissionService } from './permission.service';

describe('PermissionService', () => {
  let roles: string[];
  let permissions: PermissionService;

  beforeEach(() => {
    roles = [];
    TestBed.configureTestingModule({
      providers: [
        PermissionService,
        {
          provide: AuthService,
          useValue: {
            hasAnyRole: (...required: string[]) => required.some((role) => roles.includes(role)),
          },
        },
      ],
    });
    permissions = TestBed.inject(PermissionService);
  });

  it('gives ADMIN every management permission', () => {
    roles = ['ADMIN'];
    expect(permissions.canManageContracts()).toBe(true);
    expect(permissions.canManageAssignments()).toBe(true);
    expect(permissions.canManagePersons()).toBe(true);
  });

  it('limits CONTRACT_MANAGER to contract and assignment management', () => {
    roles = ['CONTRACT_MANAGER'];
    expect(permissions.canManageContracts()).toBe(true);
    expect(permissions.canManageAssignments()).toBe(true);
    expect(permissions.canManagePersons()).toBe(false);
    expect(permissions.canViewNotifications()).toBe(true);
  });

  it('keeps VIEWER read-only and INSPECTOR out of global areas', () => {
    roles = ['VIEWER'];
    expect(permissions.canManageContracts()).toBe(false);
    expect(permissions.canViewDashboard()).toBe(true);

    roles = ['INSPECTOR'];
    expect(permissions.canManageContracts()).toBe(false);
    expect(permissions.canViewDashboard()).toBe(false);
    expect(permissions.canViewNotifications()).toBe(false);
  });
});
