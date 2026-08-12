import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { BACKEND_URL } from '../../../core/config/api.config';
import { ContractAssignment, ContractAssignmentPayload } from '../models/contract-assignment';

@Injectable({ providedIn: 'root' })
export class ContractAssignmentService {
  private readonly http = inject(HttpClient);
  private readonly rootUrl = `${inject(BACKEND_URL)}/api/v1/contracts`;
  private url(contractId: string) { return `${this.rootUrl}/${contractId}/assignments`; }

  list(contractId: string) { return this.http.get<ContractAssignment[]>(this.url(contractId)); }
  create(contractId: string, payload: ContractAssignmentPayload) {
    return this.http.post<ContractAssignment>(this.url(contractId), payload);
  }
  update(contractId: string, assignmentId: string, payload: ContractAssignmentPayload) {
    return this.http.put<ContractAssignment>(`${this.url(contractId)}/${assignmentId}`, payload);
  }
  end(contractId: string, assignmentId: string, endDate: string) {
    const params = new HttpParams().set('endDate', endDate);
    return this.http.delete<ContractAssignment>(`${this.url(contractId)}/${assignmentId}`, { params });
  }
}
