package com.behpardakht.oauth_server.authorization.service.otp;

import com.behpardakht.oauth_server.authorization.model.dto.otp.response.OtpResponse;
import com.behpardakht.oauth_server.authorization.service.UserService;
import com.behpardakht.oauth_server.authorization.sms.ISmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.maskPhoneNumber;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final UserService userService;
    private final ISmsService iSmsService;
    private final OtpStorageService otpStorageService;

    @Value("${sms.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    @Value("${sms.otp.rate-limit-minutes:1}")
    private int rateLimitMinutes;

    private final SecureRandom secureRandom = new SecureRandom();

    public OtpResponse sendOtp(String phoneNumber) {
        try {
            if (otpStorageService.isRateLimited(phoneNumber)) {
                log.warn("Rate limit exceeded for phone: {}", maskPhoneNumber(phoneNumber));
                return OtpResponse.rateLimited("Rate limit exceeded. Please try again later.");
            }
            if (otpStorageService.hasValidOtp(phoneNumber)) {
                log.info("Valid OTP already exists for phone: {}", maskPhoneNumber(phoneNumber));
                return OtpResponse.alreadySent("OTP already sent. Please check your messages or wait before requesting a new one.");
            }
            if (!userService.existUserWithUsername(phoneNumber)) {
                userService.createUserByPhoneNumber(phoneNumber);
            }
            String otp = String.valueOf(10000 + secureRandom.nextInt(90000)); // Generates number between 10000-99999
            sendSms(phoneNumber, otp, otpExpirationMinutes);
            otpStorageService.storeOtp(phoneNumber, otp, otpExpirationMinutes, rateLimitMinutes);
            log.info("OTP generated and sent successfully to: {}", maskPhoneNumber(phoneNumber));
            return OtpResponse.success("OTP sent successfully to " + maskPhoneNumber(phoneNumber));
        } catch (Exception e) {
            log.error("Failed to generate OTP for phone: {}", maskPhoneNumber(phoneNumber), e);
            return OtpResponse.error("Failed to send OTP. Please try again.");
        }
    }


    public void sendSms(String phoneNumber, String otp, int otpExpirationMinutes) {
        try {
            iSmsService.send(phoneNumber, otp, otpExpirationMinutes);
            log.info("OTP SMS sent successfully. To: {}", maskPhoneNumber(phoneNumber));
        } catch (Exception e) {
            log.error("Failed to send OTP SMS to {}: {}", maskPhoneNumber(phoneNumber), e.getMessage(), e);
            throw new RuntimeException("Failed to send OTP SMS", e);
        }
    }
}