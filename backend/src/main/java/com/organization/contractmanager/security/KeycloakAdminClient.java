package com.organization.contractmanager.security;

import java.util.List;
import java.util.Set;

public interface KeycloakAdminClient {

    List<KeycloakUser> findUsers(String search, int first, int max);

    long countUsers(String search);

    KeycloakUser replaceRealmRoles(String userId, Set<ApplicationRole> roles);
}
