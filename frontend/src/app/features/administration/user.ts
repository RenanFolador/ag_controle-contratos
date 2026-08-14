import { PageResponse } from '../contracts/models/contract';

export type ApplicationRole = 'ADMIN' | 'CONTRACT_MANAGER' | 'INSPECTOR' | 'VIEWER';

export interface AdminUser {
  id: string;
  username: string;
  firstName: string | null;
  lastName: string | null;
  email: string | null;
  enabled: boolean;
  roles: ApplicationRole[];
}

export type AdminUserPage = PageResponse<AdminUser>;
