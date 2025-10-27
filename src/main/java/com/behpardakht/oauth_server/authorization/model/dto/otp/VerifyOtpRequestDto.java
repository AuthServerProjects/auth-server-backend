package com.behpardakht.oauth_server.authorization.model.dto.otp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyOtpRequestDto {

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^\\d{5}$", message = "OTP must be exactly 5 digits")
    private String otp;
}