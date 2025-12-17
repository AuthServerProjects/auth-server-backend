package com.behpardakht.oauth_server.authorization.security.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.Serializable;

@Component
public class ClientPermissionEvaluator implements PermissionEvaluator {

    private static final String CLIENT_HEADER = "X-Client-Id";

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }

        // SuperAdmin bypass
        if (hasRole(authentication, "ROLE_SUPER_ADMIN")) {
            return true;
        }

        String clientId = getClientIdFromHeader();
        if (clientId == null) {
            return false;
        }

        String requiredAuthority = clientId + ":" + permission.toString();
        return hasAuthority(authentication, requiredAuthority);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        // For object-level permission (future use)
        return hasPermission(authentication, null, permission);
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(authority));
    }

    private boolean hasRole(Authentication authentication, String role) {
        return hasAuthority(authentication, role);
    }

    private String getClientIdFromHeader() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        return request.getHeader(CLIENT_HEADER);
    }
}