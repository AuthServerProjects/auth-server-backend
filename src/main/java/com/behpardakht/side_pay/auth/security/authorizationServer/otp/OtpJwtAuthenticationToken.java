package com.behpardakht.side_pay.auth.security.authorizationServer.otp;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;

@Getter
public class OtpJwtAuthenticationToken extends JwtAuthenticationToken {

    private final String phoneNumber;
    private final String otp;

    public OtpJwtAuthenticationToken(Jwt jwt,
                                     Collection<? extends GrantedAuthority> authorities,
                                     String name,
                                     String phoneNumber,
                                     String otp) {
        super(jwt, authorities, name);
        this.phoneNumber = phoneNumber;
        this.otp = otp;
    }

    @Override
    public Object getPrincipal() {
        return phoneNumber;
    }

    @Override
    public Object getCredentials() {
        return otp;
    }
}