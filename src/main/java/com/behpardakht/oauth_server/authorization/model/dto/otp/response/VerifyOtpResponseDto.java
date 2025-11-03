package com.behpardakht.oauth_server.authorization.model.dto.otp.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyOtpResponseDto {
    String state;
    String redirectUri;
    String authorizationCode;
    String phoneNumber;
}