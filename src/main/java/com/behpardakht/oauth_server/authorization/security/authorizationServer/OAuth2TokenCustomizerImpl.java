package com.behpardakht.oauth_server.authorization.security.authorizationServer;

import com.behpardakht.oauth_server.authorization.model.entity.Client;
import com.behpardakht.oauth_server.authorization.model.entity.Permission;
import com.behpardakht.oauth_server.authorization.model.entity.UserRoleAssignment;
import com.behpardakht.oauth_server.authorization.service.ClientService;
import com.behpardakht.oauth_server.authorization.service.MetricsService;
import com.behpardakht.oauth_server.authorization.service.user.UserRoleAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class OAuth2TokenCustomizerImpl implements OAuth2TokenCustomizer<JwtEncodingContext> {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String CLIENT_ID_CLAIM = "client-id";
    private static final String CLIENT_DB_ID_CLAIM = "client-db-id";
    private static final String ROLES_CLAIM = "roles";
    private static final String AUTHORITIES_CLAIM = "authorities";

    private final ClientService clientService;
    private final MetricsService metricsService;
    private final UserRoleAssignmentService userRoleAssignmentService;

    @Override
    public void customize(JwtEncodingContext context) {
        String clientId = context.getRegisteredClient().getClientId();
        Client client = clientService.findByClientId(clientId);

        context.getClaims().claim(CLIENT_ID_CLAIM, clientId);
        context.getClaims().claim(CLIENT_DB_ID_CLAIM, client.getId());

        if (ACCESS_TOKEN.equals(context.getTokenType().getValue())) {
            String username = context.getPrincipal().getName();
            Set<String> roles = new HashSet<>();
            Set<String> authorities = new HashSet<>();
            List<UserRoleAssignment> assignments = userRoleAssignmentService.findByUsernameAndClientId(username, client.getId());
            for (UserRoleAssignment assignment : assignments) {
                roles.add(assignment.getRole().getName());
                for (Permission permission : assignment.getRole().getPermissions()) {
                    authorities.add(permission.getName());
                }
            }
            context.getClaims().claim(ROLES_CLAIM, roles);
            context.getClaims().claim(AUTHORITIES_CLAIM, authorities);
            metricsService.incrementTokenIssued(clientId, context.getAuthorizationGrantType().getValue());
        }
        if (REFRESH_TOKEN.equals(context.getTokenType().getValue())) {
            metricsService.incrementTokenRefreshed(clientId);
        }
    }
}