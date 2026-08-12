import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { BACKEND_URL } from '../../../core/config/api.config';
import { Person } from '../models/person';
import { PersonService } from './person.service';

describe('PersonService', () => {
  let service: PersonService;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting(),
      { provide: BACKEND_URL, useValue: 'http://backend' }] });
    service = TestBed.inject(PersonService); http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('searches persons by name', () => {
    service.list('Maria').subscribe();
    const request = http.expectOne(req => req.url === 'http://backend/api/v1/persons');
    expect(request.request.params.get('name')).toBe('Maria');
    request.flush([]);
  });

  it('deactivates using a logical update without audit fields', () => {
    const person: Person = { id:'id-1', name:'Maria', cpf:null, registration:'10',
      email:'maria@example.com', phone:null, whatsappEnabled:false, active:true,
      createdAt:'2026-01-01T00:00:00Z', updatedAt:'2026-01-01T00:00:00Z' };
    service.deactivate(person).subscribe();
    const request = http.expectOne('http://backend/api/v1/persons/id-1');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body.active).toBe(false);
    expect(request.request.body.createdAt).toBeUndefined();
    request.flush({ ...person, active:false });
  });
});
