package com.behpardakht.oauth_server.authorization.model.dto.otp.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SendOtpRequestDto extends BaseOtpRequestDto {

    @NotBlank(message = "{phone_number_is_required}")
    @Pattern(regexp = "^98\\d{10}$", message = "{invalid_phone_number_format}")
    private String phoneNumber;
}