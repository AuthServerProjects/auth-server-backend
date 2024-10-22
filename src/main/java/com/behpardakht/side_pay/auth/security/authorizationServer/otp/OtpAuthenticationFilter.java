package com.behpardakht.side_pay.auth.security.authorizationServer.otp;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@AllArgsConstructor
public class OtpAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final OtpJwtAuthenticationConverter otpJwtAuthenticationConverter;
    private final OTPAuthenticationProvider otpAuthenticationProvider;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        Authentication authentication = otpJwtAuthenticationConverter.convert(request);
        return otpAuthenticationProvider.authenticate(authentication);
    }
}