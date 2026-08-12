declare const DOCKER_KEYCLOAK_URL: string;
declare const DOCKER_KEYCLOAK_REALM: string;
declare const DOCKER_KEYCLOAK_CLIENT_ID: string;

export const environment = {
  production: true,
  backendUrl: '',
  keycloak: {
    url: DOCKER_KEYCLOAK_URL,
    realm: DOCKER_KEYCLOAK_REALM,
    clientId: DOCKER_KEYCLOAK_CLIENT_ID,
  },
};
