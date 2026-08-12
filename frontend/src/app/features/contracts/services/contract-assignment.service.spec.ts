import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { BACKEND_URL } from '../../../core/config/api.config';
import { ContractAssignmentService } from './contract-assignment.service';

describe('ContractAssignmentService', () => {
  let service: ContractAssignmentService;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting(),
      { provide: BACKEND_URL, useValue: 'http://backend' }] });
    service = TestBed.inject(ContractAssignmentService); http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('allows creating multiple independent assignments through the collection endpoint', () => {
    const payload = { personId: 'person-1', role: 'MANAGER' as const,
      startDate: '2026-01-01', endDate: null };
    service.create('contract-1', payload).subscribe();
    const request = http.expectOne('http://backend/api/v1/contracts/contract-1/assignments');
    expect(request.request.method).toBe('POST'); expect(request.request.body).toEqual(payload);
    request.flush({});
  });

  it('ends an assignment using DELETE and the selected final date', () => {
    service.end('contract-1', 'assignment-1', '2026-08-12').subscribe();
    const request = http.expectOne(req => req.url.endsWith('/assignment-1'));
    expect(request.request.method).toBe('DELETE');
    expect(request.request.params.get('endDate')).toBe('2026-08-12');
    request.flush({});
  });
});
