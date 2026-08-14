import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { BACKEND_URL } from '../../../core/config/api.config';
import { ContractService } from './contract.service';

describe('ContractService', () => {
  let service: ContractService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [
      provideHttpClient(), provideHttpClientTesting(),
      { provide: BACKEND_URL, useValue: 'http://backend' }
    ] });
    service = TestBed.inject(ContractService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends pagination, sorting and search to the real contracts endpoint', () => {
    service.list({ page: 2, size: 10, sort: 'endDate,desc', search: '025' }).subscribe();
    const request = http.expectOne(req => req.url === 'http://backend/api/v1/contracts');
    expect(request.request.params.get('page')).toBe('2');
    expect(request.request.params.get('size')).toBe('10');
    expect(request.request.params.get('sort')).toBe('endDate,desc');
    expect(request.request.params.get('search')).toBe('025');
    request.flush({ content: [], page: 2, size: 10, totalElements: 0,
      totalPages: 0, first: false, last: true });
  });

  it('uses the close endpoint', () => {
    service.close('contract-id').subscribe();
    const request = http.expectOne('http://backend/api/v1/contracts/contract-id/close');
    expect(request.request.method).toBe('POST');
    request.flush({});
  });

  it('sends renewal data to the dedicated endpoint', () => {
    const payload = { newEndDate: '2027-12-31', reason: 'Prorrogação',
      reference: '1º Termo Aditivo', notes: null };
    service.renew('contract-id', payload).subscribe();
    const request = http.expectOne('http://backend/api/v1/contracts/contract-id/renew');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush({});
  });

  it('loads contract history', () => {
    service.history('contract-id').subscribe();
    const request = http.expectOne('http://backend/api/v1/contracts/contract-id/history');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });
});
