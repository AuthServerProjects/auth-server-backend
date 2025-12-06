package com.behpardakht.oauth_server.authorization.model.dto.otp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpResponseDto {
    private String state;
    private String redirectUri;
    private String authorizationCode;
    private String phoneNumber;
}