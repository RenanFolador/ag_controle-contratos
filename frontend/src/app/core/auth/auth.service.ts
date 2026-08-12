import { inject, Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly keycloak = inject(Keycloak);

  get authenticated(): boolean {
    return this.keycloak.authenticated === true;
  }

  get userName(): string {
    return (
      (this.keycloak.tokenParsed?.['preferred_username'] as string) ??
      (this.keycloak.tokenParsed?.['name'] as string) ??
      ''
    );
  }

  login(returnUrl = window.location.href): Promise<void> {
    return this.keycloak.login({ redirectUri: returnUrl });
  }

  logout(): Promise<void> {
    return this.keycloak.logout({ redirectUri: window.location.origin });
  }

  async getAccessToken(): Promise<string | undefined> {
    if (!this.authenticated) {
      return undefined;
    }
    await this.keycloak.updateToken(30);
    return this.keycloak.token;
  }
}
