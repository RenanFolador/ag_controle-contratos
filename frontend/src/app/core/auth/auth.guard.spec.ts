import { Router } from '@angular/router';
import { TestBed } from '@angular/core/testing';
import { requireAuthentication } from './auth.guard';

describe('authGuard authentication flow', () => {
  it('redirects unauthenticated navigation to the local login page', async () => {
    const loginTree = { route: 'login' };
    const router = { createUrlTree: vi.fn().mockReturnValue(loginTree) };
    TestBed.configureTestingModule({ providers: [{ provide: Router, useValue: router }] });

    const result = await TestBed.runInInjectionContext(() =>
      requireAuthentication({}, { url: '/contracts' }, { authenticated: false } as never),
    );

    expect(result).toBe(loginTree);
    expect(router.createUrlTree).toHaveBeenCalledWith(['/login'], {
      queryParams: { returnUrl: '/contracts' },
    });
  });

  it('allows an authenticated navigation', async () => {
    TestBed.configureTestingModule({ providers: [{ provide: Router, useValue: {} }] });

    const result = await TestBed.runInInjectionContext(() =>
      requireAuthentication({}, { url: '/dashboard' }, { authenticated: true } as never),
    );

    expect(result).toBe(true);
  });
});
