package com.behpardakht.oauth_server.authorization.security.authorizationServer;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class OAuth2TokenCustomizerImpl implements OAuth2TokenCustomizer<JwtEncodingContext> {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String CLIENT_ID_CLAIM = "client-id";
    private static final String ROLES_CLAIM = "roles";

    @Override
    public void customize(JwtEncodingContext context) {
        String clientId = context.getRegisteredClient().getClientId();
        context.getClaims().claim(CLIENT_ID_CLAIM, clientId);

        if (ACCESS_TOKEN.equals(context.getTokenType().getValue())) {
            List<String> roles = context.getPrincipal().getAuthorities().stream()
                    .filter(Objects::nonNull).map(GrantedAuthority::getAuthority).toList();

            context.getClaims().claim(ROLES_CLAIM, roles);
        }
    }
}