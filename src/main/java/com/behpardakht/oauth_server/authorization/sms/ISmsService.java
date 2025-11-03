package com.behpardakht.oauth_server.authorization.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ISmsService {

    public void send(String phoneNumber, String otp, int otpExpirationMinutes) {
        log.info("Sending OTP : {} to {}", otp, phoneNumber);
    }
}