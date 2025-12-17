package com.behpardakht.oauth_server.authorization.util;

import com.behpardakht.oauth_server.authorization.model.dto.base.BaseFilterDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.function.Supplier;

public class SecurityUtils {
    public static Long getCurrentClientId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String clientId = request.getHeader("X-Client-Id");
        return clientId != null ? Long.parseLong(clientId) : null;
    }

    public static boolean isSuperAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    public static <T extends BaseFilterDto> void setClientContext(PageableRequestDto<T> request,
                                                                  Supplier<T> filterSupplier) {
        if (request.getFilters() == null) {
            request.setFilters(filterSupplier.get());
        }
        request.getFilters().setClientId(getCurrentClientId());
    }
}