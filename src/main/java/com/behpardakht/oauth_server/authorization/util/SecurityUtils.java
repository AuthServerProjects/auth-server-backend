package com.behpardakht.oauth_server.authorization.util;

import com.behpardakht.oauth_server.authorization.exception.ExceptionMessage;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.NotFoundException;
import com.behpardakht.oauth_server.authorization.model.dto.base.BaseFilterDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.model.entity.Client;
import com.behpardakht.oauth_server.authorization.service.ClientService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final ClientService clientService;

    public static Long getCurrentClientId() {
        HttpServletRequest request = getCurrentRequest();
        String clientId = request.getHeader("X-Client-Id");
        if (clientId == null || clientId.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(clientId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Client getCurrentClient() {
        HttpServletRequest request = getCurrentRequest();
        String clientDbIdHeader = request.getHeader("X-Client-Db-Id");
        if (StringUtils.hasText(clientDbIdHeader)) {
            try {
                Long clientDbId = Long.parseLong(clientDbIdHeader);
                return clientService.findById(clientDbId);
            } catch (NumberFormatException e) {
                // Invalid number, try next header
            }
        }
        String clientIdHeader = request.getHeader("X-Client-Id");
        if (StringUtils.hasText(clientIdHeader)) {
            return clientService.findByClientId(clientIdHeader);
        }
        throw new NotFoundException("Client", "ClientId", clientIdHeader);
    }

    private static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("No request context available");
        }
        return attributes.getRequest();
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
        if (SecurityUtils.isSuperAdmin()) {
            return;
        }
        Long currentClientId = SecurityUtils.getCurrentClientId();
        if (!clientId.equals(currentClientId)) {
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

    public static <T extends BaseFilterDto> void setClientContext(PageableRequestDto<T> request,
                                                                  Supplier<T> filterSupplier) {
        if (request.getFilters() == null) {
            request.setFilters(filterSupplier.get());
        }
        request.getFilters().setClientId(getCurrentClientId());
    }
}