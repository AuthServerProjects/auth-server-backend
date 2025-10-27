package com.behpardakht.side_pay.auth.model.dto.otp;

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