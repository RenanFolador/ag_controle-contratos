import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router, provideRouter } from '@angular/router';
import { apiErrorInterceptor } from './api-error.interceptor';
import { LoadingService } from './loading.service';
import { HttpClient } from '@angular/common/http';

describe('apiErrorInterceptor', () => {
  const snackBar = { open: vi.fn() };

  beforeEach(() => {
    snackBar.open.mockReset();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([apiErrorInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: MatSnackBar, useValue: snackBar },
      ],
    });
  });

  it('shows a friendly conflict message and clears loading', () => {
    const http = TestBed.inject(HttpClient);
    const controller = TestBed.inject(HttpTestingController);
    const loading = TestBed.inject(LoadingService);

    http.post('/api/resource', {}).subscribe({ error: () => undefined });
    expect(loading.active()).toBe(true);
    controller
      .expectOne('/api/resource')
      .flush({ message: 'technical database detail' }, { status: 409, statusText: 'Conflict' });

    expect(snackBar.open).toHaveBeenCalledWith(
      'A operação conflita com um registro existente.',
      'Fechar',
      { duration: 6000 },
    );
    expect(loading.active()).toBe(false);
  });

  it('navigates to the application login page on 401', async () => {
    const http = TestBed.inject(HttpClient);
    const controller = TestBed.inject(HttpTestingController);
    const router = TestBed.inject(Router);
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    http.get('/api/resource').subscribe({ error: () => undefined });
    controller.expectOne('/api/resource').flush(null, { status: 401, statusText: 'Unauthorized' });
    await Promise.resolve();
    expect(navigate).toHaveBeenCalledWith(['/login'], {
      queryParams: { returnUrl: '/' },
    });
  });
});
