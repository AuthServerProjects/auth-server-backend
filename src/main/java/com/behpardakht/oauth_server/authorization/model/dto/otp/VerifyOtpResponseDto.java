package com.behpardakht.oauth_server.authorization.model.dto.otp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyOtpResponseDto {
    private String state;
    private String redirectUri;
    private String authorizationCode;
    private String phoneNumber;
}