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
      onLoad: 'check-sso',
      flow: 'standard',
      pkceMethod: 'S256',
      silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
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
