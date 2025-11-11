package com.behpardakht.oauth_server.authorization.model.dto.otp.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BaseOtpRequestDto {

    @NotBlank(message = "{state_is_required}")
    @Size(min = 43, max = 500, message = "{invalid_state_size}")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "{invalid_state_format}")
    private String state;
}