import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { BACKEND_URL } from '../../core/config/api.config';
import { UserAdminService } from './user.service';

describe('UserAdminService', () => {
  let service: UserAdminService;
  let controller: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        UserAdminService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: BACKEND_URL, useValue: 'http://backend' },
      ],
    });
    service = TestBed.inject(UserAdminService);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => controller.verify());

  it('lists users with pagination and search', () => {
    service.list(1, 20, 'maria').subscribe();

    const request = controller.expectOne(
      (item) => item.url === 'http://backend/api/v1/admin/users'
        && item.params.get('search') === 'maria',
    );
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('page')).toBe('1');
    expect(request.request.params.get('size')).toBe('20');
    request.flush({ content: [], page: 1, size: 20, totalElements: 0, totalPages: 0, first: false, last: true });
  });

  it('updates application roles', () => {
    service.updateRoles('user-1', ['ADMIN', 'VIEWER']).subscribe();

    const request = controller.expectOne('http://backend/api/v1/admin/users/user-1/roles');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ roles: ['ADMIN', 'VIEWER'] });
    request.flush({});
  });
});
