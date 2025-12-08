package com.behpardakht.oauth_server.authorization.service.otp;

import com.behpardakht.oauth_server.authorization.config.Properties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.maskPhoneNumber;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpStorageService {

    private final Properties properties;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String OTP_KEY = "otp:";
    private static final String OTP_ATTEMPT_KEY = "otp_attempt:";
    private static final String VERIFICATION_ATTEMPT_KEY = "verify_attempt:";

    private static final String PHONE_RATE_LIMIT_KEY = "phone_rate_limit:";
    private static final String IP_RATE_LIMIT_KEY = "ip_rate_limit:";
    private static final String IP_OTP_COUNT_KEY = "ip_otp_count:";
    private static final String GLOBAL_OTP_COUNT_KEY = "global_otp_count";
    private static final String SESSION_KEY = "session:";

    private static final String CLIENT_ID = "client_id";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String CODE_CHALLENGE = "code_challenge";
    private static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
    private static final String SCOPE = "scope";
    private static final String PHONE_NUMBER = "phone_number";

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public boolean isPhoneNumberRateLimited(String phoneNumber) {
        String key = PHONE_RATE_LIMIT_KEY + phoneNumber;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public boolean isIpRateLimited(String ipAddress) {
        String key = IP_RATE_LIMIT_KEY + ipAddress;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public boolean isGlobalRateLimited() {
        Integer count = (Integer) redisTemplate.opsForValue().get(GLOBAL_OTP_COUNT_KEY);
        if (count == null) {
            return false;
        }
        try {
            return count >= properties.getStorage().getTimes().getMaxGlobalOtpPerMinute();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean hasValidOtp(String phoneNumber) {
        String key = OTP_KEY + phoneNumber;
        OtpData otpData = deserializeOtpData(redisTemplate.opsForValue().get(key));
        if (otpData == null) {
            return false;
        }
        return Instant.now().isBefore(otpData.expirationTime());
    }

    public void storeOtp(String phoneNumber, String otp, String ipAddress) {
        int otpTime = properties.getStorage().getExpirationTimeMin().getOtp();
        String key = OTP_KEY + phoneNumber;
        OtpData otpData = new OtpData(otp, Instant.now().plusSeconds(otpTime * 60L));
        redisTemplate.opsForValue().set(key, otpData, Duration.ofMinutes(otpTime));
        log.debug("OTP stored for phone: {}, expires in {} minutes", maskPhoneNumber(phoneNumber), otpTime);
        setPhoneNumberRateLimit(phoneNumber, properties.getStorage().getExpirationTimeMin().getRateLimit());
        setIpRateLimit(ipAddress);
        trackGlobalOtpRequest();
    }

    public void setPhoneNumberRateLimit(String phoneNumber, int minutes) {
        String key = PHONE_RATE_LIMIT_KEY + phoneNumber;
        redisTemplate.opsForValue().set(key, "rate_limited", Duration.ofMinutes(minutes));
        log.info("Rate limit set for phone: {} for {} minutes", maskPhoneNumber(phoneNumber), minutes);
    }

    private void setIpRateLimit(String ipAddress) {
        String key = IP_OTP_COUNT_KEY + ipAddress;
        redisTemplate.opsForValue().setIfAbsent(key, 0L, Duration.ofHours(1));
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count >= properties.getStorage().getTimes().getMaxOtpPerIpPerHour()) {
            String rateLimitKey = IP_RATE_LIMIT_KEY + ipAddress;
            redisTemplate.opsForValue().set(rateLimitKey, "blocked", Duration.ofHours(1));
            log.warn("IP {} blocked due to {} OTP requests in 1 hour", ipAddress, count);
        }
    }

    private void trackGlobalOtpRequest() {
        redisTemplate.opsForValue().setIfAbsent(GLOBAL_OTP_COUNT_KEY, 0L, Duration.ofMinutes(1));
        Long count = redisTemplate.opsForValue().increment(GLOBAL_OTP_COUNT_KEY);
        if (count != null && count > properties.getStorage().getTimes().getMaxGlobalOtpPerMinute()) {
            log.warn("Global OTP rate limit exceeded: {} requests in 1 minute", count);
        }
    }

    public boolean validateAndConsumeOtp(String phoneNumber, String otp, String ipAddress) {
        if (isVerificationRateLimited(phoneNumber, ipAddress)) {
            log.warn("Verification rate limit exceeded for phone: {} from IP: {}",
                    maskPhoneNumber(phoneNumber), ipAddress);
            return false;
        }
        String key = OTP_KEY + phoneNumber;
        if (!validateOtp(phoneNumber, otp, key, ipAddress)) {
            return false;
        }
        redisTemplate.delete(key);
        redisTemplate.delete(OTP_ATTEMPT_KEY + phoneNumber);
        redisTemplate.delete(VERIFICATION_ATTEMPT_KEY + phoneNumber + ":" + ipAddress);

        log.info("OTP successfully validated and consumed for phone: {}", maskPhoneNumber(phoneNumber));
        return true;
    }

    private boolean isVerificationRateLimited(String phoneNumber, String ipAddress) {
        String key = VERIFICATION_ATTEMPT_KEY + phoneNumber + ":" + ipAddress;
        Integer count = (Integer) redisTemplate.opsForValue().get(key);
        if (count == null) {
            return false;
        }
        try {
            return count >= properties.getStorage().getTimes().getMaxVerificationAttemptsPerHour();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean validateOtp(String phoneNumber, String otp, String key, String ipAddress) {
        OtpData otpData = deserializeOtpData(redisTemplate.opsForValue().get(key));
        if (otpData == null) {
            log.warn("No OTP found for phone: {}", maskPhoneNumber(phoneNumber));
            trackVerificationAttempt(phoneNumber, ipAddress);
            return false;
        }
        if (Instant.now().isAfter(otpData.expirationTime())) {
            redisTemplate.delete(key);
            log.warn("Expired OTP attempted for phone: {}", maskPhoneNumber(phoneNumber));
            return false;
        }
        if (!otp.equals(otpData.otpCode())) {
            incrementFailedAttempts(phoneNumber);
            trackVerificationAttempt(phoneNumber, ipAddress);
            log.warn("Invalid OTP attempted for phone: {} from IP: {}",
                    maskPhoneNumber(phoneNumber), ipAddress);
            return false;
        }
        return true;
    }

    private void trackVerificationAttempt(String phoneNumber, String ipAddress) {
        String key = VERIFICATION_ATTEMPT_KEY + phoneNumber + ":" + ipAddress;
        redisTemplate.opsForValue().setIfAbsent(key, 0L, Duration.ofHours(1));
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count >= properties.getStorage().getTimes().getMaxVerificationAttemptsPerHour()) {
            log.warn("Phone {} verification blocked from IP {} due to {} failed attempts",
                    maskPhoneNumber(phoneNumber), ipAddress, count);
        }
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
        String key = OTP_ATTEMPT_KEY + phoneNumber;
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts != null) {
            if (attempts == 1) {
                redisTemplate.expire(key, properties.getStorage().getExpirationTimeMin().getLockAccount(), TimeUnit.MINUTES);
            }
            if (attempts >= properties.getStorage().getTimes().getFailedAttempts()) {
                int lockDuration = Math.min((int) Math.pow(2, attempts), 60);
                setPhoneNumberRateLimit(phoneNumber, lockDuration);
                log.warn("Phone {} blocked for {} minutes due to {} failed OTP attempts",
                        maskPhoneNumber(phoneNumber), lockDuration, attempts);
            }
        }
    }

    //--------------------------------------------------------------------------

    public boolean stateExists(String state) {
        if (state == null || state.isEmpty()) {
            return false;
        }
        String key = SESSION_KEY + state;
        Boolean exists = redisTemplate.hasKey(key);
        if (!Boolean.TRUE.equals(exists)) {
            log.warn("State not found or expired: {}", state);
            return false;
        }
        return true;
    }

    public void storeOAuth2Parameters(String clientId, String state, String redirectUri,
                                      String codeChallenge, String codeChallengeMethod, String scope) {
        String key = SESSION_KEY + state;
        Duration expiration = Duration.ofMinutes(properties.getStorage().getExpirationTimeMin().getInitialize());

        Map<String, String> sessionData = new HashMap<>();
        sessionData.put(CLIENT_ID, clientId);
        sessionData.put(REDIRECT_URI, redirectUri != null ? redirectUri : "");
        sessionData.put(CODE_CHALLENGE, codeChallenge != null ? codeChallenge : "");
        sessionData.put(CODE_CHALLENGE_METHOD, codeChallengeMethod != null ? codeChallengeMethod : "");
        sessionData.put(SCOPE, scope != null ? scope : "");

        redisTemplate.opsForHash().putAll(key, sessionData);
        redisTemplate.expire(key, expiration);
    }

    public void storePhoneNumber(String state, String phoneNumber) {
        String key = SESSION_KEY + state;
        redisTemplate.opsForHash().put(key, PHONE_NUMBER, phoneNumber);
    }

    public String getPhoneNumber(String state) {
        String key = SESSION_KEY + state;
        return (String) redisTemplate.opsForHash().get(key, PHONE_NUMBER);
    }

    public SessionDto getSessionDto(String state) {
        String key = SESSION_KEY + state;
        Map<Object, Object> data = redisTemplate.opsForHash().entries(key);
        return new SessionDto(
                (String) data.get(CLIENT_ID),
                (String) data.get(REDIRECT_URI),
                (String) data.get(CODE_CHALLENGE),
                (String) data.get(CODE_CHALLENGE_METHOD),
                (String) data.get(SCOPE),
                (String) data.get(PHONE_NUMBER),
                state
        );
    }

    public void markStateAsConsumed(String state) {
        String key = SESSION_KEY + state;
        redisTemplate.delete(key);
        log.debug("State marked as consumed and cleaned up: {}", state);
    }

    public record OtpData(String otpCode, Instant expirationTime) {
    }

    public record SessionDto(String clientId, String redirectUri, String codeChallenge,
                             String codeChallengeMethod, String scope, String phoneNumber, String state) {
    }
}