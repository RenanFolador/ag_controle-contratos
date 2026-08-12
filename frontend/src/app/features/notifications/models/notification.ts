import { PageResponse } from '../../contracts/models/contract';

export type NotificationChannel = 'EMAIL' | 'WHATSAPP';
export type NotificationStatus = 'PENDING' | 'SENT' | 'FAILED' | 'CANCELLED';

export interface Notification {
  id: string;
  contractId: string;
  contractNumber: string;
  companyName: string;
  personId: string;
  recipientName: string;
  recipientAddress: string;
  channel: NotificationChannel;
  daysBefore: number;
  expirationDate: string;
  scheduledDate: string;
  status: NotificationStatus;
  sentAt: string | null;
  errorMessage: string | null;
  retryCount: number;
  createdAt: string;
}

export interface NotificationFilters {
  page: number;
  size: number;
  status?: NotificationStatus | '';
  channel?: NotificationChannel | '';
  contract?: string;
  date?: string;
}

export type NotificationPage = PageResponse<Notification>;
