import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { BACKEND_URL } from '../../../core/config/api.config';
import { Person, PersonPayload } from '../models/person';

@Injectable({ providedIn: 'root' })
export class PersonService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${inject(BACKEND_URL)}/api/v1/persons`;

  list(name?: string) {
    const params = name?.trim() ? new HttpParams().set('name', name.trim()) : undefined;
    return this.http.get<Person[]>(this.baseUrl, { params });
  }
  getById(id: string) { return this.http.get<Person>(`${this.baseUrl}/${id}`); }
  create(payload: PersonPayload) {
    return this.http.post<Person>(this.baseUrl, {
      name: payload.name, cpf: payload.cpf, registration: payload.registration,
      email: payload.email, phone: payload.phone,
      whatsappEnabled: payload.whatsappEnabled
    });
  }
  update(id: string, payload: PersonPayload) { return this.http.put<Person>(`${this.baseUrl}/${id}`, payload); }
  deactivate(person: Person) {
    return this.update(person.id, {
      name: person.name, cpf: person.cpf, registration: person.registration,
      email: person.email, phone: person.phone,
      whatsappEnabled: person.whatsappEnabled, active: false
    });
  }
}
