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

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            Long clientId = Long.parseLong(request.getParameter("client_id"));
            ClientContextHolder.setClientId(clientId);
            chain.doFilter(request, response);
        } finally {
            ClientContextHolder.clear();
        }
    }
}