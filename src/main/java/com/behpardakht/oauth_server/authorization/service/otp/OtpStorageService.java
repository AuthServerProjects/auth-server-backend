package com.behpardakht.oauth_server.authorization.service.otp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.maskPhoneNumber;

@Service
@AllArgsConstructor
@Slf4j
public class OtpStorageService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String OTP_PREFIX = "otp:";
    private static final String OTP_ATTEMPT_PREFIX = "otp_attempt:";
    private static final String AUTH_CODE_PREFIX = "auth_code:";
    private static final String AUTH_SESSION_PREFIX = "auth_session:";
    private static final String OTP_RATE_LIMIT_PREFIX = "otp_rate_limit:";

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public boolean isRateLimited(String phoneNumber) {
        String key = OTP_RATE_LIMIT_PREFIX + phoneNumber;
        return redisTemplate.hasKey(key);
    }

    public boolean hasValidOtp(String phoneNumber) {
        String key = OTP_PREFIX + phoneNumber;
        OtpData otpData = deserializeOtpData(redisTemplate.opsForValue().get(key));
        if (otpData == null) {
            return false;
        }
        return Instant.now().isBefore(otpData.expirationTime());
    }

    public void storeOtp(String phoneNumber, String otp, int expirationMinutes, int rateLimitMinutes) {
        String key = OTP_PREFIX + phoneNumber;
        OtpData otpData = new OtpData(otp, Instant.now().plusSeconds(expirationMinutes * 60L));
        redisTemplate.opsForValue().set(key, otpData, Duration.ofMinutes(expirationMinutes));
        log.debug("OTP stored for phone: {}, expires in {} minutes", maskPhoneNumber(phoneNumber), expirationMinutes);
        setRateLimit(phoneNumber, rateLimitMinutes);
    }

    public void setRateLimit(String phoneNumber, int minutes) {
        String key = OTP_RATE_LIMIT_PREFIX + phoneNumber;
        redisTemplate.opsForValue().set(key, "rate_limited", Duration.ofMinutes(minutes));
        log.info("Rate limit set for phone: {} for {} minutes", maskPhoneNumber(phoneNumber), minutes);
    }

    public boolean validateAndConsumeOtp(String phoneNumber, String otp) {
        String key = OTP_PREFIX + phoneNumber;
        if (validateOtp(phoneNumber, otp, key)) {
            return false;
        }
        redisTemplate.delete(key);
        redisTemplate.delete(OTP_ATTEMPT_PREFIX + phoneNumber);
        log.info("OTP successfully validated and consumed for phone: {}", maskPhoneNumber(phoneNumber));
        return true;
    }

    private boolean validateOtp(String phoneNumber, String otp, String key) {
        OtpData otpData = deserializeOtpData(redisTemplate.opsForValue().get(key));
        if (otpData == null) {
            log.warn("No OTP found for phone: {}", maskPhoneNumber(phoneNumber));
            return true;
        }
        if (Instant.now().isAfter(otpData.expirationTime())) {
            redisTemplate.delete(key);
            log.warn("Expired OTP attempted for phone: {}", maskPhoneNumber(phoneNumber));
            return true;
        }
        if (!otp.equals(otpData.otpCode())) {
            incrementFailedAttempts(phoneNumber);
            log.warn("Invalid OTP attempted for phone: {}", maskPhoneNumber(phoneNumber));
            return true;
        }
        return false;
    }

    private void incrementFailedAttempts(String phoneNumber) {
        String key = OTP_ATTEMPT_PREFIX + phoneNumber;
        Long attempts = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 15, TimeUnit.MINUTES);

        if (attempts != null && attempts >= 3) {
            setRateLimit(phoneNumber, 15); // Block for 15 minutes after 3 failed attempts
            log.warn("Phone {} blocked due to {} failed OTP attempts", maskPhoneNumber(phoneNumber), attempts);
        }
    }

    public void storeAuthSessionId(String authSessionId, String phoneNumber, int expirationMinutes) {
        String key = AUTH_SESSION_PREFIX + authSessionId;
        redisTemplate.opsForValue().set(key, phoneNumber, Duration.ofMinutes(expirationMinutes));
        log.debug("Auth Session stored: {}", authSessionId);
    }

    public void storeAuthCode(String authCode, String phoneNumber, int expirationMinutes) {
        String key = AUTH_CODE_PREFIX + authCode;
        redisTemplate.opsForValue().set(key, phoneNumber, Duration.ofMinutes(expirationMinutes));
        log.debug("Auth Code stored: {}", authCode);
    }

    public String getPhoneNumberByAuthSessionId(String authSessionId) {
        String key = AUTH_SESSION_PREFIX + authSessionId;
        return (String) redisTemplate.opsForValue().get(key);
    }

    public String getPhoneNumberByAuthCodeId(String authCode) {
        String key = AUTH_CODE_PREFIX + authCode;
        return (String) redisTemplate.opsForValue().get(key);
    }

    public void removeAuthSessionId(String authSessionId) {
        String key = AUTH_SESSION_PREFIX + authSessionId;
        redisTemplate.delete(key);
    }

    public void removeAuthCode(String authCode) {
        String key = AUTH_CODE_PREFIX + authCode;
        redisTemplate.delete(key);
    }

    private OtpData deserializeOtpData(Object rawData) {
        if (rawData == null) return null;
        try {
            return objectMapper.convertValue(rawData, OtpData.class);
        } catch (Exception e) {
            log.error("Failed to deserialize OtpData: {}", e.getMessage());
            return null;
        }
    }

    public record OtpData(String otpCode, Instant expirationTime) {
    }
}