package com.behpardakht.oauth_server.authorization.model.dto.otp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VerifyOtpRequestDto extends BaseOtpRequestDto {

    @NotBlank(message = "{otp_is_required}")
    @Pattern(regexp = "^\\d{6}$", message = "{invalid_otp_size}")
    private String otp;
}