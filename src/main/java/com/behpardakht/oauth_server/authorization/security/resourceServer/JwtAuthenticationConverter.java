package com.behpardakht.oauth_server.authorization.security.resourceServer;

import jakarta.validation.constraints.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@SuppressWarnings("unchecked")
public class JwtAuthenticationConverter implements Converter<Jwt, CustomJwtAuthenticationToken> {

    @Override
    public CustomJwtAuthenticationToken convert(@NotNull Jwt jwt) {
        List<GrantedAuthority> authorities = Stream.of(roles(jwt), scopes(jwt), authorities(jwt))
                .flatMap(s -> s)
                .toList();
        String clientId = getCustomField(jwt, "client-id");
        return new CustomJwtAuthenticationToken(jwt, authorities, jwt.getSubject(), clientId);
    }

    private String getCustomField(Jwt jwt, String fieldName) {
        Object value = jwt.getClaims().get(fieldName);
        return value != null ? String.valueOf(value) : null;
    }

    private Stream<GrantedAuthority> roles(Jwt jwt) {
        List<String> roles = (List<String>) jwt.getClaims().getOrDefault("roles", List.of());
        return Optional.ofNullable(roles).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(role -> new SimpleGrantedAuthority("ROLE_" + role));
    }

    private Stream<GrantedAuthority> scopes(Jwt jwt) {
        List<String> scopes = (List<String>) jwt.getClaims().getOrDefault("scope", List.of());
        return Optional.ofNullable(scopes).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope));
    }

    private Stream<GrantedAuthority> authorities(Jwt jwt) {
        List<String> authorities = (List<String>) jwt.getClaims().getOrDefault("authorities", List.of());
        return Optional.ofNullable(authorities).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(SimpleGrantedAuthority::new);
    }
}