package com.behpardakht.side_pay.auth.security;

import com.behpardakht.side_pay.auth.security.token.OtpJwtAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.stereotype.Component;

@Component
public class OtpJwtAuthenticationConverter implements AuthenticationConverter {

    @Override
    public Authentication convert(HttpServletRequest request) {
        String grantType = request.getParameter("grant_type");

        if (grantType.equals("otp")) {
            String phoneNumber = request.getParameter("phoneNumber");
            String otp = request.getParameter("otp");
            return new OtpJwtAuthenticationToken(null, null, null, phoneNumber, otp);
        }
        return null;
    }
}