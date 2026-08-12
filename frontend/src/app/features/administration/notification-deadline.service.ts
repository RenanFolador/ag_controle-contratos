import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { BACKEND_URL } from '../../core/config/api.config';
import { NotificationDeadline } from './notification-deadline';
@Injectable({ providedIn: 'root' })
export class NotificationDeadlineService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${inject(BACKEND_URL)}/api/v1/admin/notification-deadlines`;
  list() {
    return this.http.get<NotificationDeadline[]>(this.baseUrl);
  }
  create(daysBefore: number, enabled: boolean) {
    return this.http.post<NotificationDeadline>(this.baseUrl, { daysBefore, enabled });
  }
  setEnabled(id: string, enabled: boolean) {
    return this.http.patch<NotificationDeadline>(`${this.baseUrl}/${id}/enabled`, { enabled });
  }
  remove(id: string) {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
