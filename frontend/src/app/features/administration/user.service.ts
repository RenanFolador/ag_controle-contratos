import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { BACKEND_URL } from '../../core/config/api.config';
import { AdminUser, AdminUserPage, ApplicationRole } from './user';

@Injectable({ providedIn: 'root' })
export class UserAdminService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${inject(BACKEND_URL)}/api/v1/admin/users`;

  list(page: number, size: number, search?: string) {
    let params = new HttpParams().set('page', page).set('size', size);
    if (search?.trim()) {
      params = params.set('search', search.trim());
    }
    return this.http.get<AdminUserPage>(this.baseUrl, { params });
  }

  updateRoles(userId: string, roles: ApplicationRole[]) {
    return this.http.put<AdminUser>(`${this.baseUrl}/${userId}/roles`, { roles });
  }
}
