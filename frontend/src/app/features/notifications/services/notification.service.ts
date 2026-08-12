import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { BACKEND_URL } from '../../../core/config/api.config';
import { NotificationFilters, NotificationPage } from '../models/notification';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly http = inject(HttpClient);
  private readonly url = `${inject(BACKEND_URL)}/api/v1/notifications`;

  list(filters: NotificationFilters) {
    let params = new HttpParams().set('page', filters.page).set('size', filters.size);
    if (filters.status) params = params.set('status', filters.status);
    if (filters.channel) params = params.set('channel', filters.channel);
    if (filters.contract?.trim()) params = params.set('contract', filters.contract.trim());
    if (filters.date) params = params.set('date', filters.date);
    return this.http.get<NotificationPage>(this.url, { params });
  }
}
