package com.behpardakht.oauth_server.authorization.security.resourceServer;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;

@Getter
public class CustomJwtAuthenticationToken extends JwtAuthenticationToken {

    private final String clientId;

    public CustomJwtAuthenticationToken(Jwt jwt,
                                        Collection<? extends GrantedAuthority> authorities,
                                        String name,
                                        String clientId) {
        super(jwt, authorities, name);
        this.clientId = clientId;
    }
}