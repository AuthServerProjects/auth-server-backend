package com.behpardakht.oauth_server.authorization.model.dto.otp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OtpResponse {
    private boolean success;
    private String message;
    private OtpResultType type;

    public static OtpResponse success(String message) {
        return OtpResponse.builder().success(true).message(message).type(OtpResultType.SUCCESS).build();
    }

    public static OtpResponse alreadySent(String message) {
        return OtpResponse.builder().success(true).message(message).type(OtpResultType.ALREADY_SENT).build();
    }

    public static OtpResponse rateLimited(String message) {
        return OtpResponse.builder().success(false).message(message).type(OtpResultType.RATE_LIMITED).build();
    }

    public static OtpResponse error(String message) {
        return OtpResponse.builder().success(false).message(message).type(OtpResultType.ERROR).build();
    }
}

enum OtpResultType {
    SUCCESS, RATE_LIMITED, ERROR, ALREADY_SENT
}