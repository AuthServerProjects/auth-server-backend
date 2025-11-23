package com.behpardakht.oauth_server.authorization.security.filter;

import com.behpardakht.oauth_server.authorization.service.TokenBlacklistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.getClientIpAddress;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenBlacklistFilter extends OncePerRequestFilter {

    private final TokenBlacklistService tokenBlacklistService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (tokenBlacklistService.isAccessTokenBlacklisted(token)) {
                log.warn("SECURITY ALERT: Blocked blacklisted token attempt from IP: {} to URI: {}",
                        getClientIpAddress(request), request.getRequestURI());
                sendUnauthorizedResponse(response, "Token has been revoked", "Please login again");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response,
                                          String error,
                                          String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = Map.of(
                "success", false,
                "error", error,
                "message", message,
                "status", 401,
                "timestamp", System.currentTimeMillis());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.contains("/otp/") ||
                path.contains("/oauth2/token") ||
                path.contains("/oauth2/authorize") ||
                path.contains("/.well-known/") ||
                path.contains("/oauth2/jwks");
    }
}