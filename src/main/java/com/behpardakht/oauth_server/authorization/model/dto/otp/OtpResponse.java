package com.behpardakht.oauth_server.authorization.model.dto.otp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OtpResponse {
    private boolean success;
    private String message;
    private OtpResultType type;

    public static OtpResponse success(String message) {
        return new OtpResponse(true, message, OtpResultType.SUCCESS);
    }

    public static OtpResponse alreadySent(String message) {
        return new OtpResponse(true, message, OtpResultType.ALREADY_SENT);
    }

    public static OtpResponse rateLimited(String message) {
        return new OtpResponse(false, message, OtpResultType.RATE_LIMITED);
    }

    public static OtpResponse error(String message) {
        return new OtpResponse(false, message, OtpResultType.ERROR);
    }
}

enum OtpResultType {
    SUCCESS, RATE_LIMITED, ERROR, ALREADY_SENT
}