package com.behpardakht.oauth_server.authorization.security.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Order(-100)
@Component
public class OAuth2RedirectFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if ("/oauth2/authorize".equals(httpRequest.getRequestURI()) &&
                HttpMethod.GET.name().equals(httpRequest.getMethod()) &&
                "code".equals(httpRequest.getParameter("response_type"))) {

            log.info("Intercepting OAuth2 authorization request, redirecting to OTP flow");

            StringBuilder redirectUrl = new StringBuilder("/otp/enterPhoneNumber");
            redirectUrl.append("?client_id=").append(httpRequest.getParameter("client_id"));

            addParameterIfPresent(redirectUrl, "redirect_uri", httpRequest.getParameter("redirect_uri"));
            addParameterIfPresent(redirectUrl, "state", httpRequest.getParameter("state"));
            addParameterIfPresent(redirectUrl, "code_challenge", httpRequest.getParameter("code_challenge"));
            addParameterIfPresent(redirectUrl, "code_challenge_method", httpRequest.getParameter("code_challenge_method"));
            addParameterIfPresent(redirectUrl, "scope", httpRequest.getParameter("scope"));

            httpResponse.sendRedirect(redirectUrl.toString());
            return;
        }
        chain.doFilter(request, response);
    }

    private void addParameterIfPresent(StringBuilder url, String paramName, String paramValue) {
        if (paramValue != null && !paramValue.trim().isEmpty()) {
            try {
                url.append("&").append(paramName).append("=")
                        .append(URLEncoder.encode(paramValue, StandardCharsets.UTF_8));
            } catch (Exception e) {
                log.warn("Failed to encode parameter {}: {}", paramName, paramValue);
            }
        }
    }
}