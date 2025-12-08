package com.behpardakht.oauth_server.authorization.security.resourceServer;

import com.behpardakht.oauth_server.authorization.config.bundle.MessageResolver;
import com.behpardakht.oauth_server.authorization.exception.ExceptionMessage;
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
import java.util.Arrays;

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
        ExceptionMessage exceptionType = detectExceptionType(fullMessage);
        String message = MessageResolver.getMessage(exceptionType.getMessage());
        return ResponseDto.failed(exceptionType.getMessage(), message, null);
    }

    private ExceptionMessage detectExceptionType(String fullMessage) {
        if (containsAny(fullMessage, "expired", "jwt expired")) {
            return ExceptionMessage.TOKEN_EXPIRED;
        }
        if (containsAny(fullMessage, "signature", "no matching key", "another algorithm expected", "invalid signature")) {
            return ExceptionMessage.TOKEN_INVALID_SIGNATURE;
        }
        if (containsAny(fullMessage, "not yet valid", "before", "nbf")) {
            return ExceptionMessage.TOKEN_NOT_VALID;
        }
        if (containsAny(fullMessage, "malformed", "invalid jwt", "cannot decode")) {
            return ExceptionMessage.TOKEN_MALFORMED;
        }
        if (containsAny(fullMessage, "bearer token", "missing", "credentials not found")) {
            return ExceptionMessage.TOKEN_MISSING;
        }
        if (containsAny(fullMessage, "issuer", "iss")) {
            return ExceptionMessage.TOKEN_INVALID_ISSUER;
        }
        if (containsAny(fullMessage, "audience", "aud")) {
            return ExceptionMessage.TOKEN_INVALID_AUDIENCE;
        }
        return ExceptionMessage.AUTHENTICATION_FAILED;
    }

    private boolean containsAny(String text, String... keywords) {
        return Arrays.stream(keywords).anyMatch(text::contains);
    }
}