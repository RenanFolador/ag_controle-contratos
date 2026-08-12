import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { BACKEND_URL } from '../../core/config/api.config';

export type ReportType =
  | 'ACTIVE_CONTRACTS'
  | 'EXPIRED_CONTRACTS'
  | 'EXPIRING_CONTRACTS'
  | 'CONTRACTS_BY_RESPONSIBLE'
  | 'SENT_NOTIFICATIONS'
  | 'FAILED_NOTIFICATIONS';

export interface ReportFilters {
  type: ReportType;
  format: 'CSV';
  year?: number | null;
  status?: string | null;
  startDate?: string | null;
  endDate?: string | null;
  personId?: string | null;
  company?: string | null;
}

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly http = inject(HttpClient);
  private readonly url = `${inject(BACKEND_URL)}/api/v1/reports/export`;

  export(filters: ReportFilters) {
    let params = new HttpParams().set('type', filters.type).set('format', filters.format);
    Object.entries(filters).forEach(([key, value]) => {
      if (
        key !== 'type' &&
        key !== 'format' &&
        value !== null &&
        value !== undefined &&
        value !== ''
      ) {
        params = params.set(key, String(value));
      }
    });
    return this.http.get(this.url, {
      params,
      observe: 'response',
      responseType: 'blob',
    });
  }

  download(response: HttpResponse<Blob>): void {
    const disposition = response.headers.get('content-disposition') ?? '';
    const filename = /filename="?([^";]+)"?/i.exec(disposition)?.[1] ?? 'relatorio.csv';
    const url = URL.createObjectURL(response.body ?? new Blob());
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = filename;
    anchor.click();
    URL.revokeObjectURL(url);
  }
}
