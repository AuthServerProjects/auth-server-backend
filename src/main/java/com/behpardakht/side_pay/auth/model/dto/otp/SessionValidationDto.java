package com.behpardakht.side_pay.auth.model.dto.otp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SessionValidationDto {
    private boolean valid;
    private String phoneNumber;
    private String errorMessage;

    public static SessionValidationDto success(String phoneNumber) {
        return new SessionValidationDto(true, phoneNumber, null);
    }

    public static SessionValidationDto failure(String errorMessage) {
        return new SessionValidationDto(false, null, errorMessage);
    }
}