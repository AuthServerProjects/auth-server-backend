package com.behpardakht.oauth_server.authorization.security.resourceServer;

import com.behpardakht.oauth_server.authorization.config.bundle.MessageResolver;
import com.behpardakht.oauth_server.authorization.exception.Messages;
import com.behpardakht.oauth_server.authorization.model.dto.base.ResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.warn("Authentication failed for request {}: {}", request.getRequestURI(), authException.getMessage());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ResponseDto<?> responseDto = determineErrorType(authException);
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
    }

    private ResponseDto<?> determineErrorType(AuthenticationException authException) {
        String exceptionMessage = authException.getMessage();
        String causeMessage = authException.getCause() != null ? authException.getCause().getMessage() : "";
        String fullMessage = (exceptionMessage + " " + causeMessage).toLowerCase();
        Messages exceptionType = detectExceptionType(fullMessage);
        String message = MessageResolver.getMessage(exceptionType.getMessage());
        return ResponseDto.failed(exceptionType.getMessage(), message, null);
    }

    private Messages detectExceptionType(String fullMessage) {
        if (containsAny(fullMessage, "expired", "jwt expired")) {
            return Messages.TOKEN_EXPIRED;
        }
        if (containsAny(fullMessage, "signature", "no matching key", "another algorithm expected", "invalid signature")) {
            return Messages.TOKEN_INVALID_SIGNATURE;
        }
        if (containsAny(fullMessage, "not yet valid", "before", "nbf")) {
            return Messages.TOKEN_NOT_VALID;
        }
        if (containsAny(fullMessage, "malformed", "invalid jwt", "cannot decode")) {
            return Messages.TOKEN_MALFORMED;
        }
        if (containsAny(fullMessage, "bearer token", "missing", "credentials not found")) {
            return Messages.TOKEN_MISSING;
        }
        if (containsAny(fullMessage, "issuer", "iss")) {
            return Messages.TOKEN_INVALID_ISSUER;
        }
        if (containsAny(fullMessage, "audience", "aud")) {
            return Messages.TOKEN_INVALID_AUDIENCE;
        }
        return Messages.AUTHENTICATION_FAILED;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}