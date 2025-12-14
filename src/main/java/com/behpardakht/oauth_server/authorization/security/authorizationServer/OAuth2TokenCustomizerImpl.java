package com.behpardakht.oauth_server.authorization.security.authorizationServer;

import com.behpardakht.oauth_server.authorization.model.entity.Permission;
import com.behpardakht.oauth_server.authorization.model.entity.RoleAssignment;
import com.behpardakht.oauth_server.authorization.repository.RoleAssignmentRepository;
import com.behpardakht.oauth_server.authorization.service.MetricsService;
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
    private static final String ROLES_CLAIM = "roles";
    private static final String AUTHORITIES_CLAIM = "authorities";

    private final MetricsService metricsService;
    private final RoleAssignmentRepository roleAssignmentRepository;

    @Override
    public void customize(JwtEncodingContext context) {
        String clientId = context.getRegisteredClient().getClientId();
        context.getClaims().claim(CLIENT_ID_CLAIM, clientId);
        if (ACCESS_TOKEN.equals(context.getTokenType().getValue())) {
            Set<String> roles = new HashSet<>();
            Set<String> authorities = new HashSet<>();
            convertAssignmentToAuthority(roles, authorities, context.getPrincipal().getName());
            context.getClaims().claim(ROLES_CLAIM, roles);
            context.getClaims().claim(AUTHORITIES_CLAIM, authorities);
            metricsService.incrementTokenIssued(clientId, context.getAuthorizationGrantType().getValue());
        }
        if (REFRESH_TOKEN.equals(context.getTokenType().getValue())) {
            metricsService.incrementTokenRefreshed(clientId);
        }
    }

    private void convertAssignmentToAuthority(Set<String> roles, Set<String> authorities, String username) {
        List<RoleAssignment> assignments = roleAssignmentRepository.findByUserUsername(username);
        for (RoleAssignment assignment : assignments) {
            String roleName = assignment.getRole().getName();
            roles.add(roleName);
            String clientPrefix = assignment.getClient().getClientId() + ":";
            for (Permission permission : assignment.getRole().getPermissions()) {
                authorities.add(clientPrefix + permission.getName());
            }
        }
    }
}