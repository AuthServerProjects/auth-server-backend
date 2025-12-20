package com.behpardakht.oauth_server.authorization.security.filter;

import com.behpardakht.oauth_server.authorization.security.ClientContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ClientContextFilter extends OncePerRequestFilter {

    private static final String CLIENT_ID_HEADER = "X-Client-Id";
    private static final String CLIENT_ID_PARAM = "client_id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            Long clientId = extractClientId(request);
            if (clientId != null) {
                ClientContextHolder.setClientId(clientId);
            }
            chain.doFilter(request, response);
        } finally {
            ClientContextHolder.clear();
        }
    }

    private Long extractClientId(HttpServletRequest request) {
        String headerValue = request.getHeader(CLIENT_ID_HEADER);
        if (headerValue != null && !headerValue.isBlank()) {
            try {
                return Long.parseLong(headerValue.trim());
            } catch (NumberFormatException e) {
                // Invalid header value, try parameter
            }
        }

        String paramValue = request.getParameter(CLIENT_ID_PARAM);
        if (paramValue != null && !paramValue.isBlank()) {
            try {
                return Long.parseLong(paramValue.trim());
            } catch (NumberFormatException e) {
                // Invalid parameter value, return null
            }
        }
        return null;
    }
}