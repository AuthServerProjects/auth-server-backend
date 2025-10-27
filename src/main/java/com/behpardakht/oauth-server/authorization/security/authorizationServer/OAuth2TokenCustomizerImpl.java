package com.behpardakht.side_pay.auth.security.authorizationServer;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class OAuth2TokenCustomizerImpl implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Override
    public void customize(JwtEncodingContext context) {
        String clientId = context.getRegisteredClient().getClientId();
        context.getClaims().claim("client-id", clientId);

        if (context.getTokenType().getValue().equals("access_token")) {
            List<String> authorization =
                    context.getPrincipal().getAuthorities().stream()
                            .filter(Objects::nonNull).map(GrantedAuthority::getAuthority).toList();

            context.getClaims().claim("roles", authorization);
        }
    }
}