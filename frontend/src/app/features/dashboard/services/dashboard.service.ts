import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { BACKEND_URL } from '../../../core/config/api.config';
import { DashboardMetrics } from '../models/dashboard';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly url = `${inject(BACKEND_URL)}/api/v1/dashboard`;
  getDashboard() { return this.http.get<DashboardMetrics>(this.url); }
}
