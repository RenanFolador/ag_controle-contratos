import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { BACKEND_URL } from '../../../core/config/api.config';
import { Contract, ContractPayload, ContractQuery, ContractSummary, PageResponse } from '../models/contract';

@Injectable({ providedIn: 'root' })
export class ContractService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${inject(BACKEND_URL)}/api/v1/contracts`;

  list(query: ContractQuery) {
    let params = new HttpParams()
      .set('page', query.page)
      .set('size', query.size)
      .set('sort', query.sort);
    if (query.search?.trim()) params = params.set('search', query.search.trim());
    if (query.expirationDays !== undefined) params = params.set('expirationDays', query.expirationDays);
    return this.http.get<PageResponse<ContractSummary>>(this.baseUrl, { params });
  }

  getById(id: string) { return this.http.get<Contract>(`${this.baseUrl}/${id}`); }
  create(payload: ContractPayload) { return this.http.post<Contract>(this.baseUrl, payload); }
  update(id: string, payload: ContractPayload) { return this.http.put<Contract>(`${this.baseUrl}/${id}`, payload); }
  close(id: string) { return this.http.post<Contract>(`${this.baseUrl}/${id}/close`, {}); }
  cancel(id: string) { return this.http.post<Contract>(`${this.baseUrl}/${id}/cancel`, {}); }
}
