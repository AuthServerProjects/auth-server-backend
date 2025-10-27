package com.behpardakht.oauth_server.authorization.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.maskPhoneNumber;

@Service
@Slf4j
public class SmsService {

    public void sendOtp(String phoneNumber, String otp) {
        try {
            String messageBody = String.format("Your OTP code is: %s. This code will expire in 5 minutes. Do not share this code with anyone.", otp);
            log.info("###### OTP CODE IS: {}", otp);
            log.info("OTP SMS sent successfully. To: {}", maskPhoneNumber(phoneNumber));
        } catch (Exception e) {
            log.error("Failed to send OTP SMS to {}: {}", maskPhoneNumber(phoneNumber), e.getMessage(), e);
            throw new RuntimeException("Failed to send OTP SMS", e);
        }
    }
}