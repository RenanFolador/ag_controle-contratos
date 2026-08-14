import { PageResponse } from '../contracts/models/contract';

export type ApplicationRole = 'ADMIN' | 'CONTRACT_MANAGER' | 'INSPECTOR' | 'VIEWER';

export interface ApplicationRoleOption {
  value: ApplicationRole;
  label: string;
  description: string;
}

export const APPLICATION_ROLE_OPTIONS: ApplicationRoleOption[] = [
  {
    value: 'ADMIN',
    label: 'Administrador',
    description: 'Possui acesso completo ao sistema, incluindo configurações e gerenciamento de usuários.',
  },
  {
    value: 'CONTRACT_MANAGER',
    label: 'Gestor de contratos',
    description: 'Pode cadastrar e alterar contratos e gerenciar seus responsáveis.',
  },
  {
    value: 'INSPECTOR',
    label: 'Fiscal',
    description: 'Pode acessar os contratos relacionados à sua fiscalização.',
  },
  {
    value: 'VIEWER',
    label: 'Visualização',
    description: 'Possui acesso somente às funcionalidades de consulta permitidas.',
  },
];

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
