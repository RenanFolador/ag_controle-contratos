import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { BACKEND_URL } from '../config/api.config';

@Injectable({ providedIn: 'root' })
export class ApiClientService {
  private readonly http = inject(HttpClient);
  private readonly backendUrl = inject(BACKEND_URL);

  get<T>(path: string) {
    return this.http.get<T>(`${this.backendUrl}${path}`);
  }
}
