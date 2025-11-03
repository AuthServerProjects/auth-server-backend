package com.behpardakht.oauth_server.authorization.security.resourceServer;

import jakarta.validation.constraints.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Component
public class JwtAuthenticationConverter implements Converter<Jwt, CustomJwtAuthenticationToken> {

    @Override
    public CustomJwtAuthenticationToken convert(@NotNull Jwt jwt) {
        List<GrantedAuthority> authorities = Stream.concat(roles(jwt), scopes(jwt)).toList();
        String clientId = getCustomField(jwt, "client-id");
        return new CustomJwtAuthenticationToken(jwt, authorities, null, clientId);
    }

    private String getCustomField(Jwt jwt, String filedName) {
        return String.valueOf(jwt.getClaims().get(filedName));
    }

    private Stream<GrantedAuthority> roles(Jwt jwt) {
        List<String> roles = (List<String>) jwt.getClaims().getOrDefault("roles", List.of());
        if (roles.isEmpty()) {
            return Stream.empty();
        }
        return roles.stream().filter(Objects::nonNull)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role));
    }

    private Stream<GrantedAuthority> scopes(Jwt jwt) {
        List<String> scopes = (List<String>) jwt.getClaims().getOrDefault("scope", List.of());
        if (scopes.isEmpty()) {
            return Stream.empty();
        }
        return scopes.stream().filter(Objects::nonNull)
                .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope));
    }
}