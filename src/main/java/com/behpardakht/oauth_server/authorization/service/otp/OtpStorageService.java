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
    private static final String OTP_RATE_LIMIT_PREFIX = "otp_rate_limit:";
    private static final String AUTH_CODE_PREFIX = "auth_code:";

    private static final String CLIENT_ID_PREFIX = "client_id:";
    private static final String REDIRECT_URI_PREFIX = "redirect_uri:";
    private static final String CODE_CHALLENGE_PREFIX = "code_challenge:";
    private static final String CODE_CHALLENGE_METHOD_PREFIX = "code_challenge_method:";
    private static final String SCOPE_PREFIX = "scope:";
    private static final String PHONE_NUMBER_PREFIX = "phone_number:";

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public boolean isRateLimited(String phoneNumber) {
        String key = OTP_RATE_LIMIT_PREFIX + phoneNumber;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
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
        if (!validateOtp(phoneNumber, otp, key)) {
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
            return false;
        }
        if (Instant.now().isAfter(otpData.expirationTime())) {
            redisTemplate.delete(key);
            log.warn("Expired OTP attempted for phone: {}", maskPhoneNumber(phoneNumber));
            return false;
        }
        if (!otp.equals(otpData.otpCode())) {
            incrementFailedAttempts(phoneNumber);
            log.warn("Invalid OTP attempted for phone: {}", maskPhoneNumber(phoneNumber));
            return false;
        }
        return true;
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

    private void incrementFailedAttempts(String phoneNumber) {
        String key = OTP_ATTEMPT_PREFIX + phoneNumber;
        Long attempts = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 15, TimeUnit.MINUTES);

        if (attempts != null && attempts >= 3) {
            setRateLimit(phoneNumber, 15); // Block for 15 minutes after 3 failed attempts
            log.warn("Phone {} blocked due to {} failed OTP attempts", maskPhoneNumber(phoneNumber), attempts);
        }
    }

    //--------------------------------------------------------------------------

    public void storeOAuth2Parameters(String clientId, String state, String redirectUri,
                                      String codeChallenge, String codeChallengeMethod, String scope) {
        redisTemplate.opsForValue().set(CLIENT_ID_PREFIX + state, clientId);
        redisTemplate.opsForValue().set(REDIRECT_URI_PREFIX + state, redirectUri);
        redisTemplate.opsForValue().set(CODE_CHALLENGE_PREFIX + state, codeChallenge);
        redisTemplate.opsForValue().set(CODE_CHALLENGE_METHOD_PREFIX + state, codeChallengeMethod);
        redisTemplate.opsForValue().set(SCOPE_PREFIX + state, scope);
    }

    public void storePhoneNumber(String state, String phoneNumber, int expirationMinutes) {
        String key = PHONE_NUMBER_PREFIX + state;
        redisTemplate.opsForValue().set(key, phoneNumber, Duration.ofMinutes(expirationMinutes));
        log.debug("Phone Number stored: {}", state);
    }

    public void removePhoneNumberByAuthSessionId(String state) {
        String key = PHONE_NUMBER_PREFIX + state;
        redisTemplate.delete(key);
    }

    public void storeAuthCode(String authCode, String phoneNumber, int expirationMinutes) {
        String key = AUTH_CODE_PREFIX + authCode;
        redisTemplate.opsForValue().set(key, phoneNumber, Duration.ofMinutes(expirationMinutes));
        log.debug("Auth Code stored: {}", authCode);
    }


    public String getPhoneNumber(String state) {
        return (String) redisTemplate.opsForValue().get(PHONE_NUMBER_PREFIX + state);
    }

    public SessionDto getSessionDto(String state) {
        return new SessionDto(
                (String) redisTemplate.opsForValue().get(CLIENT_ID_PREFIX + state),
                (String) redisTemplate.opsForValue().get(REDIRECT_URI_PREFIX + state),
                (String) redisTemplate.opsForValue().get(CODE_CHALLENGE_PREFIX + state),
                (String) redisTemplate.opsForValue().get(CODE_CHALLENGE_METHOD_PREFIX + state),
                (String) redisTemplate.opsForValue().get(SCOPE_PREFIX + state),
                (String) redisTemplate.opsForValue().get(PHONE_NUMBER_PREFIX + state),
                state
        );
    }

    public record OtpData(String otpCode, Instant expirationTime) {
    }

    public record SessionDto(String clientId, String redirectUri, String codeChallenge,
                             String codeChallengeMethod, String scope, String phoneNumber, String state) {
    }
}