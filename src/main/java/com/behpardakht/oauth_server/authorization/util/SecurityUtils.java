package com.behpardakht.oauth_server.authorization.util;

import com.behpardakht.oauth_server.authorization.exception.ExceptionMessage;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper;
import com.behpardakht.oauth_server.authorization.security.ClientContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {
        // Utility class
    }

    public static boolean isSuperAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    public static void validateOwnership(Long clientId) {
        if (isSuperAdmin()) {
            return;
        }
        Long currentClientDbId = ClientContextHolder.getClientDbId();
        if (!clientId.equals(currentClientDbId)) {
            throw new ExceptionWrapper.CustomException(ExceptionMessage.ACCESS_DENIED);
        }
    }

    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        return auth.getName();
    }
}