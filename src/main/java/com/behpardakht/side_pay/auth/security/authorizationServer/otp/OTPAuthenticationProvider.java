package com.behpardakht.side_pay.auth.security.authorizationServer.otp;

import com.behpardakht.side_pay.auth.exception.ExceptionWrapper.IncorrectException;
import com.behpardakht.side_pay.auth.model.dto.UsersDto;
import com.behpardakht.side_pay.auth.service.OtpService;
import com.behpardakht.side_pay.auth.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class OTPAuthenticationProvider implements AuthenticationProvider {

    private final OtpService otpService;
    private final JwtEncoder jwtEncoder;

    private final UserService userService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OtpJwtAuthenticationToken otpAuthentication = (OtpJwtAuthenticationToken) authentication;
        String phoneNumber = otpAuthentication.getPhoneNumber();
        String otp = otpAuthentication.getOtp();

        UsersDto users = userService.findByPhoneNumber(phoneNumber);

        if (otpService.validateOtp(phoneNumber, otp)) {
//            JwtClaimsSet claims = JwtClaimsSet.builder()
//                    .subject(phoneNumber)
//                    .claim("roles", "user")
//                    .build();
//            Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(claims));
            return new OtpJwtAuthenticationToken(null, users.getAuthorities(), null, phoneNumber, otp);
        } else {
            throw new IncorrectException("OTP");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OtpJwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}