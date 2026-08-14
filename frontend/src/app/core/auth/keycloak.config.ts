import { EnvironmentProviders } from '@angular/core';
import {
  AutoRefreshTokenService,
  provideKeycloak,
  UserActivityService,
  withAutoRefreshToken,
} from 'keycloak-angular';
import { environment } from '../../../environments/environment';

export function provideKeycloakAuth(): EnvironmentProviders {
  return provideKeycloak({
    config: environment.keycloak,
    initOptions: {
      // Restore an existing Keycloak SSO session after a browser refresh
      // without redirecting unauthenticated users away from the local /login
      // page. An explicit click on “Entrar com Keycloak” still starts login.
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
      checkLoginIframe: false,
      flow: 'standard',
      pkceMethod: 'S256',
    },
    features: [
      withAutoRefreshToken({
        onInactivityTimeout: 'logout',
        sessionTimeout: 15 * 60 * 1000,
      }),
    ],
    providers: [AutoRefreshTokenService, UserActivityService],
  });
}
