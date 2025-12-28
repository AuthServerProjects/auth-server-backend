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

    private static final String CLIENT_DB_ID_HEADER = "X-Client-Db-Id";
    private static final String CLIENT_ID_HEADER = "X-Client-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            extractAndSetClientContext(request);
            chain.doFilter(request, response);
        } finally {
            ClientContextHolder.clear();
        }
    }

    private void extractAndSetClientContext(HttpServletRequest request) {
        String clientDbIdHeader = request.getHeader(CLIENT_DB_ID_HEADER);
        if (clientDbIdHeader != null && !clientDbIdHeader.isBlank()) {
            try {
                Long clientDbId = Long.parseLong(clientDbIdHeader.trim());
                ClientContextHolder.setClientDbId(clientDbId);
            } catch (NumberFormatException e) {
                // Invalid client_db_id header value
            }
        }

        String clientIdHeader = request.getHeader(CLIENT_ID_HEADER);
        if (clientIdHeader != null && !clientIdHeader.isBlank()) {
            ClientContextHolder.setClientId(clientIdHeader.trim());
        }
    }
}