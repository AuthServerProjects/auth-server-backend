package com.behpardakht.side_pay.auth.model.dto.otp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SendOtpRequestDto {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^98\\d{10}$", message = "Phone number must be in format 98XXXXXXXXXX")
    private String phoneNumber;
}
