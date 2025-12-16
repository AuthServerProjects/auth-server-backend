package com.behpardakht.oauth_server.authorization.util;

import com.behpardakht.oauth_server.authorization.model.dto.base.BaseFilterDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.function.Supplier;

public class SecurityUtils {
    public static Long getCurrentClientId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) auth.getPrincipal();
        return jwt.getClaim("clientId");
    }

    public static <T extends BaseFilterDto> void setClientContext(PageableRequestDto<T> request,
                                                                  Supplier<T> filterSupplier) {
        if (request.getFilters() == null) {
            request.setFilters(filterSupplier.get());
        }
        request.getFilters().setClientId(getCurrentClientId());
    }
}