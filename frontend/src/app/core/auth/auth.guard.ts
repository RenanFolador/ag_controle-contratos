import { CanActivateFn } from '@angular/router';
import { AuthGuardData, createAuthGuard } from 'keycloak-angular';

const requireAuthentication = async (
  _route: unknown,
  state: { url: string },
  auth: AuthGuardData,
): Promise<boolean> => {
  if (auth.authenticated) {
    return true;
  }

  await auth.keycloak.login({
    redirectUri: `${window.location.origin}${state.url}`,
  });
  return false;
};

export const authGuard = createAuthGuard<CanActivateFn>(requireAuthentication);
