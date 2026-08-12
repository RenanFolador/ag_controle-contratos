export type ContractRole = 'MANAGER' | 'PRIMARY_INSPECTOR' | 'SUBSTITUTE_INSPECTOR';

export interface AssignmentPerson {
  id: string;
  name: string;
  registration: string | null;
  email: string | null;
  phone: string | null;
  whatsappEnabled: boolean;
  active: boolean;
}

export interface ContractAssignment {
  id: string;
  contractId: string;
  person: AssignmentPerson;
  role: ContractRole;
  startDate: string;
  endDate: string | null;
  active: boolean;
  createdAt: string;
  createdBy: string;
}

export interface ContractAssignmentPayload {
  personId: string;
  role: ContractRole;
  startDate: string;
  endDate: string | null;
}
