package com.behpardakht.oauth_server.authorization.model.dto.otp.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BaseOtpRequestDto {

    @NotBlank(message = "State is required")
    @Size(max = 500, message = "State must not exceed 500 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]*$", message = "State can only contain letters, numbers, underscores, dots and hyphens")
    private String state;
}
