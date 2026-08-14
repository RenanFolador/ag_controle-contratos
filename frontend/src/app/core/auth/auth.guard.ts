import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { inject } from '@angular/core';
import { AuthGuardData, createAuthGuard } from 'keycloak-angular';

export const requireAuthentication = async (
  _route: unknown,
  state: { url: string },
  auth: AuthGuardData,
): Promise<boolean | UrlTree> => {
  if (auth.authenticated) {
    return true;
  }

  const router = inject(Router);
  return router.createUrlTree(['/login'], {
    queryParams: { returnUrl: state.url },
  });
};

export const authGuard = createAuthGuard<CanActivateFn>(requireAuthentication);
