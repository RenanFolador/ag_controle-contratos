export type ContractStatus = 'ACTIVE' | 'CLOSED' | 'CANCELLED' | 'SUSPENDED';

export interface ContractSummary {
  id: string;
  contractNumber: string;
  processNumber: string | null;
  object: string;
  companyName: string;
  companyCnpj: string | null;
  startDate: string;
  endDate: string;
  initialValue: number;
  status: ContractStatus;
}

export interface Contract extends ContractSummary {
  notes: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;
}

export interface ContractPayload {
  contractNumber: string;
  processNumber: string | null;
  object: string;
  companyName: string;
  companyCnpj: string | null;
  startDate: string;
  endDate: string;
  initialValue: number;
  status: ContractStatus;
  notes: string | null;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ContractQuery {
  page: number;
  size: number;
  sort: string;
  search?: string;
  expirationDays?: number;
}
