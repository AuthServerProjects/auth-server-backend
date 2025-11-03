package com.behpardakht.oauth_server.authorization.model.dto.otp.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SendOtpRequestDto extends BaseOtpRequestDto {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^98\\d{10}$", message = "Phone number must be in format 98XXXXXXXXXX")
    private String phoneNumber;
}