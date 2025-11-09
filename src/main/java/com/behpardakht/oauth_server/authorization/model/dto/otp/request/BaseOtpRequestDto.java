package com.behpardakht.oauth_server.authorization.model.dto.otp.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BaseOtpRequestDto {

    @NotBlank(message = "State is required")
    @Size(min = 43, max = 500, message = "State must be between 43 and 500 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "State must be base64url encoded (letters, numbers, underscore, hyphen)")
    private String state;
}