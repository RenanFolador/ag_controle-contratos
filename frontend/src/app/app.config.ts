import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import {
  createInterceptorCondition,
  INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
  IncludeBearerTokenCondition,
  includeBearerTokenInterceptor,
} from 'keycloak-angular';

import { routes } from './app.routes';
import { provideKeycloakAuth } from './core/auth/keycloak.config';
import { environment } from '../environments/environment';

const escapedBackendUrl = environment.backendUrl.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
const backendRequest = createInterceptorCondition<IncludeBearerTokenCondition>({
  urlPattern: new RegExp(`^${escapedBackendUrl}/api(?:/.*)?$`, 'i'),
  bearerPrefix: 'Bearer',
  shouldUpdateToken: () => true,
});

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideKeycloakAuth(),
    provideRouter(routes),
    {
      provide: INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
      useValue: [backendRequest],
    },
    provideHttpClient(withInterceptors([includeBearerTokenInterceptor])),
    provideAnimationsAsync(),
  ],
};
