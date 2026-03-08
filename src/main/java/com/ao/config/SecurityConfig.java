package com.ao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/css/**", "/actuator/health", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/auth/**", "/api/ao", "/api/users/register", "/api/users/signup").permitAll()
                        .requestMatchers("/api/users/me/preferences").permitAll()
                        .requestMatchers("/api/v1/me").authenticated()
                        .requestMatchers("/api/v1/users/**", "/api/v1/notification-preferences/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/**").hasAnyRole("USER", "ANALYST", "ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtAuthenticationConverter())))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    Converter<Jwt, ? extends AbstractAuthenticationToken> keycloakJwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        Object realmAccessObj = jwt.getClaim("realm_access");
        if (realmAccessObj instanceof Map<?, ?> realmAccess) {
            Object rolesObj = realmAccess.get("roles");
            addRoles(authorities, rolesObj);
        }

        Object resourceAccessObj = jwt.getClaim("resource_access");
        if (resourceAccessObj instanceof Map<?, ?> resourceAccess) {
            for (Object clientAccessObj : resourceAccess.values()) {
                if (clientAccessObj instanceof Map<?, ?> clientAccess) {
                    addRoles(authorities, clientAccess.get("roles"));
                }
            }
        }

        return authorities;
    }

    private void addRoles(List<GrantedAuthority> authorities, Object rolesObj) {
        if (!(rolesObj instanceof List<?> roles)) {
            return;
        }

        for (Object roleObj : roles) {
            if (roleObj instanceof String roleName && !roleName.isBlank()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase()));
            }
        }
    }
}
