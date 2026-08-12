package com.organization.contractmanager.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/contracts/**")
                            .hasAnyRole("ADMIN", "CONTRACT_MANAGER", "INSPECTOR", "VIEWER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/contracts/**")
                            .hasAnyRole("ADMIN", "CONTRACT_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/contracts/**")
                            .hasAnyRole("ADMIN", "CONTRACT_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/contracts/**")
                            .hasAnyRole("ADMIN", "CONTRACT_MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/persons/**")
                            .hasAnyRole("ADMIN", "CONTRACT_MANAGER", "VIEWER")
                        .requestMatchers("/api/v1/persons/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/notifications/**")
                            .hasAnyRole("ADMIN", "CONTRACT_MANAGER", "VIEWER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/dashboard")
                            .hasAnyRole("ADMIN", "CONTRACT_MANAGER", "VIEWER")
                        .requestMatchers("/api/v1/**").denyAll()
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::realmRoles);
        return converter;
    }

    private Collection<GrantedAuthority> realmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null || !(realmAccess.get("roles") instanceof Collection<?> roles)) {
            return Collections.emptyList();
        }
        return roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toUnmodifiableSet());
    }
}
