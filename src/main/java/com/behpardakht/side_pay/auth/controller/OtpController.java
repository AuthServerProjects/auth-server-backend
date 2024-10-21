package com.behpardakht.side_pay.auth.controller;

import com.behpardakht.side_pay.auth.service.OtpService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/otp")
@AllArgsConstructor
public class OtpController {

    private final OtpService otpService;

    @GetMapping(path = "request")
    public String OtpToken(@RequestParam
                           @NotNull(message = "Phone Number can not be null")
                           @Pattern(regexp = "^98\\d{10}$", message = "Phone Number Format is wrong")
                           String phoneNumber) {
        return otpService.generateOtp(phoneNumber);
    }

    @GetMapping(path = "validate")
    public Boolean OtpValidate(@RequestParam
                               @NotNull(message = "Phone Number can not be null")
                               @Pattern(regexp = "^98\\d{10}$", message = "Phone Number Format is wrong")
                               String phoneNumber,

                               @RequestParam
                               @NotNull(message = "otp can not be null")
                               @Size(min = 5, max = 5, message = "otp must be 5 digits")
                               String otp) {
        return otpService.validateOtp(phoneNumber, otp);
    }
}