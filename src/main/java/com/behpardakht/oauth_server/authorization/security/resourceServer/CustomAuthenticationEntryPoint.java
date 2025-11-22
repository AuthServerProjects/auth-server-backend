package com.behpardakht.oauth_server.authorization.security.resourceServer;

import com.behpardakht.oauth_server.authorization.model.dto.ResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
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
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        log.warn("Authentication failed for request {}: {}",
                request.getRequestURI(), authException.getMessage());

        // Set response headers
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

        if (fullMessage.contains("expired") || fullMessage.contains("jwt expired")) {
            return ResponseDto.failed(
                    "TOKEN_EXPIRED",
                    "Your session has expired. Please refresh your token.",
                    null);
        }
        if (fullMessage.contains("signature")
                || fullMessage.contains("no matching key")
                || fullMessage.contains("another algorithm expected")
                || fullMessage.contains("invalid signature")) {
            return ResponseDto.failed(
                    "TOKEN_INVALID_SIGNATURE",
                    "Token signature is invalid. Please log in again.",
                    null);
        }
        if (fullMessage.contains("not yet valid")
                || fullMessage.contains("before")
                || fullMessage.contains("nbf")) {
            return ResponseDto.failed(
                    "TOKEN_NOT_YET_VALID",
                    "Token is not yet valid. Please check your system time.",
                    null);
        }
        if (fullMessage.contains("malformed")
                || fullMessage.contains("invalid jwt")
                || fullMessage.contains("cannot decode")) {
            return ResponseDto.failed(
                    "TOKEN_MALFORMED",
                    "Token format is invalid. Please log in again.",
                    null);
        }
        if (fullMessage.contains("bearer token")
                || fullMessage.contains("missing")
                || fullMessage.contains("credentials not found")) {
            return ResponseDto.failed(
                    "TOKEN_MISSING",
                    "Authentication required. Please provide a valid token.",
                    null);
        }
        if (fullMessage.contains("issuer") || fullMessage.contains("iss")) {
            return ResponseDto.failed(
                    "TOKEN_INVALID_ISSUER",
                    "Token issuer is invalid. Please log in again.",
                    null);
        }
        if (fullMessage.contains("audience") || fullMessage.contains("aud")) {
            return ResponseDto.failed(
                    "TOKEN_INVALID_AUDIENCE",
                    "Token audience is invalid. Please log in again.",
                    null);
        }
        return ResponseDto.failed(
                "AUTHENTICATION_FAILED",
                "Authentication failed. Please log in again.",
                null);
    }
}
