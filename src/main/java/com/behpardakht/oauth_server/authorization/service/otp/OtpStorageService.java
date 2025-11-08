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
import java.util.concurrent.TimeUnit;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.maskPhoneNumber;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtpStorageService {

    private final Properties properties;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String OTP_PREFIX = "otp:";
    private static final String OTP_ATTEMPT_PREFIX = "otp_attempt:";
    private static final String VERIFICATION_ATTEMPT_PREFIX = "verify_attempt:";

    private static final String PHONE_RATE_LIMIT_PREFIX = "phone_rate_limit:";
    private static final String IP_RATE_LIMIT_PREFIX = "ip_rate_limit:";
    private static final String IP_OTP_COUNT_PREFIX = "ip_otp_count:";
    private static final String GLOBAL_OTP_COUNT = "global_otp_count";

    private static final String CLIENT_ID_PREFIX = "client_id:";
    private static final String REDIRECT_URI_PREFIX = "redirect_uri:";
    private static final String CODE_CHALLENGE_PREFIX = "code_challenge:";
    private static final String CODE_CHALLENGE_METHOD_PREFIX = "code_challenge_method:";
    private static final String SCOPE_PREFIX = "scope:";
    private static final String PHONE_NUMBER_PREFIX = "phone_number:";

    private static final int MAX_OTP_PER_IP_PER_HOUR = 10;
    private static final int MAX_GLOBAL_OTP_PER_MINUTE = 100;
    private static final int MAX_VERIFICATION_ATTEMPTS_PER_HOUR = 10;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public boolean isPhoneNumberRateLimited(String phoneNumber) {
        String key = PHONE_RATE_LIMIT_PREFIX + phoneNumber;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public boolean isIpRateLimited(String ipAddress) {
        String key = IP_RATE_LIMIT_PREFIX + ipAddress;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public boolean isGlobalRateLimited() {
        Integer countStr = (Integer) redisTemplate.opsForValue().get(GLOBAL_OTP_COUNT);
        if (countStr == null) {
            return false;
        }
        try {
            return countStr >= MAX_GLOBAL_OTP_PER_MINUTE;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean hasValidOtp(String phoneNumber) {
        String key = OTP_PREFIX + phoneNumber;
        OtpData otpData = deserializeOtpData(redisTemplate.opsForValue().get(key));
        if (otpData == null) {
            return false;
        }
        return Instant.now().isBefore(otpData.expirationTime());
    }

    public void storeOtp(String phoneNumber, String otp, String ipAddress) {
        int otpTime = properties.expirationTime.getOtp();
        String key = OTP_PREFIX + phoneNumber;
        OtpData otpData = new OtpData(otp, Instant.now().plusSeconds(otpTime * 60L));
        redisTemplate.opsForValue().set(key, otpData, Duration.ofMinutes(otpTime));
        log.debug("OTP stored for phone: {}, expires in {} minutes", maskPhoneNumber(phoneNumber), otpTime);
        setPhoneNumberRateLimit(phoneNumber, properties.expirationTime.getRateLimit());
        setIpRateLimit(ipAddress);
        trackGlobalOtpRequest();
    }

    public void setPhoneNumberRateLimit(String phoneNumber, int minutes) {
        String key = PHONE_RATE_LIMIT_PREFIX + phoneNumber;
        redisTemplate.opsForValue().set(key, "rate_limited", Duration.ofMinutes(minutes));
        log.info("Rate limit set for phone: {} for {} minutes", maskPhoneNumber(phoneNumber), minutes);
    }

    private void setIpRateLimit(String ipAddress) {
        String countKey = IP_OTP_COUNT_PREFIX + ipAddress;
        Long count = redisTemplate.opsForValue().increment(countKey);
        if (count != null) {
            if (count == 1) {
                redisTemplate.expire(countKey, Duration.ofHours(1));
            }
            if (count >= MAX_OTP_PER_IP_PER_HOUR) {
                String rateLimitKey = IP_RATE_LIMIT_PREFIX + ipAddress;
                redisTemplate.opsForValue().set(rateLimitKey, "blocked", Duration.ofHours(1));
                log.warn("IP {} blocked due to {} OTP requests in 1 hour", ipAddress, count);
            }
        }
    }

    private void trackGlobalOtpRequest() {
        String key = GLOBAL_OTP_COUNT;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null) {
            if (count == 1) {
                redisTemplate.expire(key, Duration.ofMinutes(1));
            }
            if (count > MAX_GLOBAL_OTP_PER_MINUTE) {
                log.warn("Global OTP rate limit exceeded: {} requests in 1 minute", count);
            }
        }
    }

    public boolean validateAndConsumeOtp(String phoneNumber, String otp, String ipAddress) {
        if (isVerificationRateLimited(phoneNumber, ipAddress)) {
            log.warn("Verification rate limit exceeded for phone: {} from IP: {}",
                    maskPhoneNumber(phoneNumber), ipAddress);
            return false;
        }
        String key = OTP_PREFIX + phoneNumber;
        if (!validateOtp(phoneNumber, otp, key, ipAddress)) {
            return false;
        }
        redisTemplate.delete(key);
        redisTemplate.delete(OTP_ATTEMPT_PREFIX + phoneNumber);
        redisTemplate.delete(VERIFICATION_ATTEMPT_PREFIX + phoneNumber + ":" + ipAddress);

        log.info("OTP successfully validated and consumed for phone: {}", maskPhoneNumber(phoneNumber));
        return true;
    }

    private boolean isVerificationRateLimited(String phoneNumber, String ipAddress) {
        String key = VERIFICATION_ATTEMPT_PREFIX + phoneNumber + ":" + ipAddress;
        String countStr = (String) redisTemplate.opsForValue().get(key);
        if (countStr == null) {
            return false;
        }
        try {
            int count = Integer.parseInt(countStr);
            return count >= MAX_VERIFICATION_ATTEMPTS_PER_HOUR;
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
        String key = VERIFICATION_ATTEMPT_PREFIX + phoneNumber + ":" + ipAddress;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null) {
            if (count == 1) {
                redisTemplate.expire(key, Duration.ofHours(1));
            }
            if (count >= MAX_VERIFICATION_ATTEMPTS_PER_HOUR) {
                log.warn("Phone {} verification blocked from IP {} due to {} failed attempts",
                        maskPhoneNumber(phoneNumber), ipAddress, count);
            }
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
        String key = OTP_ATTEMPT_PREFIX + phoneNumber;
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts != null) {
            if (attempts == 1) {
                redisTemplate.expire(key, properties.expirationTime.getLockAccount(), TimeUnit.MINUTES);
            }
            if (attempts >= properties.expirationTime.getFailedAttempts()) {
                int lockDuration = Math.min((int) Math.pow(2, attempts), 60);
                setPhoneNumberRateLimit(phoneNumber, lockDuration);
                log.warn("Phone {} blocked for {} minutes due to {} failed OTP attempts",
                        maskPhoneNumber(phoneNumber), lockDuration, attempts);
            }
        }
    }

    //--------------------------------------------------------------------------

    public void storeOAuth2Parameters(String clientId, String state, String redirectUri,
                                      String codeChallenge, String codeChallengeMethod, String scope) {
        Duration expiration = Duration.ofMinutes(properties.expirationTime.getInitialize());
        redisTemplate.opsForValue().set(CLIENT_ID_PREFIX + state, clientId, expiration);
        redisTemplate.opsForValue().set(REDIRECT_URI_PREFIX + state, redirectUri, expiration);
        redisTemplate.opsForValue().set(CODE_CHALLENGE_PREFIX + state, codeChallenge, expiration);
        redisTemplate.opsForValue().set(CODE_CHALLENGE_METHOD_PREFIX + state, codeChallengeMethod, expiration);
        redisTemplate.opsForValue().set(SCOPE_PREFIX + state, scope, expiration);
    }

    public void storePhoneNumber(String state, String phoneNumber) {
        String key = PHONE_NUMBER_PREFIX + state;
        redisTemplate.opsForValue().set(key, phoneNumber, Duration.ofMinutes(properties.expirationTime.getPhoneNumber()));
        log.debug("Phone Number stored: {}", state);
    }

    public void removePhoneNumberByState(String state) {
        String key = PHONE_NUMBER_PREFIX + state;
        redisTemplate.delete(key);
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