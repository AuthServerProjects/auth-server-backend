package com.behpardakht.oauth_server.authorization.unitTest.security.otp;

import com.behpardakht.oauth_server.authorization.config.Properties;
import com.behpardakht.oauth_server.authorization.service.otp.OtpStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Security Tests for OTP (One-Time Password) System
 * Tests OTP generation, validation, rate limiting, and protection against
 * brute force, replay attacks, and other OTP-specific security threats
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OTP Security Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class OtpSecurityTest {

    @Mock
    private Properties properties;

    @Mock
    private Properties.Storage storage;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private OtpStorageService otpStorageService;

    private static final String TEST_PHONE = "09123456789";
    private static final String TEST_IP = "192.168.1.1";
    private static final String TEST_OTP = "123456";
    private static final String TEST_STATE = "test-state-123";

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(storage.getExpirationTimeMin().getOtp()).thenReturn(5);
        when(storage.getExpirationTimeMin().getRateLimit()).thenReturn(2);
        when(storage.getExpirationTimeMin().getLockAccount()).thenReturn(30);
        when(storage.getExpirationTimeMin().getInitialize()).thenReturn(10);
        when(storage.getExpirationTimeMin().getPhoneNumber()).thenReturn(15);

        // Use ReflectionTestUtils to set the final field
        ReflectionTestUtils.setField(properties, "expirationTime", storage.getExpirationTimeMin());
    }

    // ==================== RATE LIMITING TESTS ====================

    @Test
    @DisplayName("SUCCESS: Phone number not rate limited")
    void testIsPhoneNumberRateLimited_NotLimited() {
        // Given
        when(redisTemplate.hasKey("phone_rate_limit:" + TEST_PHONE)).thenReturn(false);

        // When
        boolean result = otpStorageService.isPhoneNumberRateLimited(TEST_PHONE);

        // Then
        assertThat(result).isFalse();
        verify(redisTemplate).hasKey("phone_rate_limit:" + TEST_PHONE);
    }

    @Test
    @DisplayName("SECURITY: Phone number is rate limited")
    void testIsPhoneNumberRateLimited_Limited() {
        // Given
        when(redisTemplate.hasKey("phone_rate_limit:" + TEST_PHONE)).thenReturn(true);

        // When
        boolean result = otpStorageService.isPhoneNumberRateLimited(TEST_PHONE);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("SECURITY: IP address rate limiting check")
    void testIsIpRateLimited_Limited() {
        // Given
        when(redisTemplate.hasKey("ip_rate_limit:" + TEST_IP)).thenReturn(true);

        // When
        boolean result = otpStorageService.isIpRateLimited(TEST_IP);

        // Then
        assertThat(result).isTrue();
        verify(redisTemplate).hasKey("ip_rate_limit:" + TEST_IP);
    }

    @Test
    @DisplayName("SECURITY: Global rate limiting - under limit")
    void testIsGlobalRateLimited_UnderLimit() {
        // Given
        when(valueOperations.get("global_otp_count")).thenReturn(50);

        // When
        boolean result = otpStorageService.isGlobalRateLimited();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("SECURITY: Global rate limiting - at limit")
    void testIsGlobalRateLimited_AtLimit() {
        // Given
        when(valueOperations.get("global_otp_count")).thenReturn(100);

        // When
        boolean result = otpStorageService.isGlobalRateLimited();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("SECURITY: Global rate limiting - exceeded")
    void testIsGlobalRateLimited_Exceeded() {
        // Given
        when(valueOperations.get("global_otp_count")).thenReturn(150);

        // When
        boolean result = otpStorageService.isGlobalRateLimited();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("EDGE: Global rate limiting - null counter")
    void testIsGlobalRateLimited_NullCounter() {
        // Given
        when(valueOperations.get("global_otp_count")).thenReturn(null);

        // When
        boolean result = otpStorageService.isGlobalRateLimited();

        // Then
        assertThat(result).isFalse();
    }

    // ==================== OTP STORAGE TESTS ====================

    @Test
    @DisplayName("SUCCESS: Store OTP with correct expiration")
    void testStoreOtp_Success() {
        // Given
        when(valueOperations.setIfAbsent(anyString(), any(), any(Duration.class))).thenReturn(true);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When
        otpStorageService.storeOtp(TEST_PHONE, TEST_OTP, TEST_IP);

        // Then
        verify(valueOperations).set(
                eq("otp:" + TEST_PHONE),
                any(OtpStorageService.OtpData.class),
                eq(Duration.ofMinutes(5))
        );
        verify(valueOperations).set(
                eq("phone_rate_limit:" + TEST_PHONE),
                eq("rate_limited"),
                eq(Duration.ofMinutes(2))
        );
    }

    @Test
    @DisplayName("SECURITY: Phone number rate limit set after OTP storage")
    void testSetPhoneNumberRateLimit_Success() {
        // Given
        int minutes = 5;

        // When
        otpStorageService.setPhoneNumberRateLimit(TEST_PHONE, minutes);

        // Then
        verify(valueOperations).set(
                eq("phone_rate_limit:" + TEST_PHONE),
                eq("rate_limited"),
                eq(Duration.ofMinutes(minutes))
        );
    }

    @Test
    @DisplayName("SECURITY: Valid OTP exists and not expired")
    void testHasValidOtp_ValidOtp() {
        // Given
        OtpStorageService.OtpData otpData = new OtpStorageService.OtpData(
                TEST_OTP,
                Instant.now().plusSeconds(300) // 5 minutes in future
        );
        when(valueOperations.get("otp:" + TEST_PHONE)).thenReturn(otpData);

        // When
        boolean result = otpStorageService.hasValidOtp(TEST_PHONE);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("SECURITY: Expired OTP detected")
    void testHasValidOtp_ExpiredOtp() {
        // Given
        OtpStorageService.OtpData otpData = new OtpStorageService.OtpData(
                TEST_OTP,
                Instant.now().minusSeconds(1) // Already expired
        );
        when(valueOperations.get("otp:" + TEST_PHONE)).thenReturn(otpData);

        // When
        boolean result = otpStorageService.hasValidOtp(TEST_PHONE);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("EDGE: No OTP exists")
    void testHasValidOtp_NoOtp() {
        // Given
        when(valueOperations.get("otp:" + TEST_PHONE)).thenReturn(null);

        // When
        boolean result = otpStorageService.hasValidOtp(TEST_PHONE);

        // Then
        assertThat(result).isFalse();
    }

    // ==================== OTP VALIDATION & CONSUMPTION TESTS ====================

    @Test
    @DisplayName("SUCCESS: Valid OTP validated and consumed")
    void testValidateAndConsumeOtp_Success() {
        // Given
        OtpStorageService.OtpData otpData = new OtpStorageService.OtpData(
                TEST_OTP,
                Instant.now().plusSeconds(300)
        );
        when(valueOperations.get("otp:" + TEST_PHONE)).thenReturn(otpData);
        when(valueOperations.get("verify_attempt:" + TEST_PHONE + ":" + TEST_IP)).thenReturn(null);

        // When
        boolean result = otpStorageService.validateAndConsumeOtp(TEST_PHONE, TEST_OTP, TEST_IP);

        // Then
        assertThat(result).isTrue();
        verify(redisTemplate).delete("otp:" + TEST_PHONE);
        verify(redisTemplate).delete("otp_attempt:" + TEST_PHONE);
        verify(redisTemplate).delete("verify_attempt:" + TEST_PHONE + ":" + TEST_IP);
    }

    @Test
    @DisplayName("FAIL: Invalid OTP code")
    void testValidateAndConsumeOtp_InvalidCode() {
        // Given
        OtpStorageService.OtpData otpData = new OtpStorageService.OtpData(
                TEST_OTP,
                Instant.now().plusSeconds(300)
        );
        when(valueOperations.get("otp:" + TEST_PHONE)).thenReturn(otpData);
        when(valueOperations.get("verify_attempt:" + TEST_PHONE + ":" + TEST_IP)).thenReturn(null);
        when(valueOperations.setIfAbsent(anyString(), any(), any(Duration.class))).thenReturn(true);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When
        boolean result = otpStorageService.validateAndConsumeOtp(TEST_PHONE, "wrong-otp", TEST_IP);

        // Then
        assertThat(result).isFalse();
        verify(redisTemplate, never()).delete("otp:" + TEST_PHONE);
    }

    @Test
    @DisplayName("SECURITY: Expired OTP rejected during validation")
    void testValidateAndConsumeOtp_ExpiredOtp() {
        // Given
        OtpStorageService.OtpData otpData = new OtpStorageService.OtpData(
                TEST_OTP,
                Instant.now().minusSeconds(1)
        );
        when(valueOperations.get("otp:" + TEST_PHONE)).thenReturn(otpData);
        when(valueOperations.get("verify_attempt:" + TEST_PHONE + ":" + TEST_IP)).thenReturn(null);

        // When
        boolean result = otpStorageService.validateAndConsumeOtp(TEST_PHONE, TEST_OTP, TEST_IP);

        // Then
        assertThat(result).isFalse();
        verify(redisTemplate).delete("otp:" + TEST_PHONE); // Expired OTP is deleted
    }

    @Test
    @DisplayName("SECURITY: No OTP found for phone number")
    void testValidateAndConsumeOtp_NoOtp() {
        // Given
        when(valueOperations.get("otp:" + TEST_PHONE)).thenReturn(null);
        when(valueOperations.get("verify_attempt:" + TEST_PHONE + ":" + TEST_IP)).thenReturn(null);
        when(valueOperations.setIfAbsent(anyString(), any(), any(Duration.class))).thenReturn(true);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When
        boolean result = otpStorageService.validateAndConsumeOtp(TEST_PHONE, TEST_OTP, TEST_IP);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("SECURITY: Verification rate limit exceeded")
    void testValidateAndConsumeOtp_VerificationRateLimited() {
        // Given
        when(valueOperations.get("verify_attempt:" + TEST_PHONE + ":" + TEST_IP)).thenReturn("10");

        // When
        boolean result = otpStorageService.validateAndConsumeOtp(TEST_PHONE, TEST_OTP, TEST_IP);

        // Then
        assertThat(result).isFalse();
        verify(valueOperations, never()).get("otp:" + TEST_PHONE);
    }

    // ==================== BRUTE FORCE PROTECTION ====================

    @Test
    @DisplayName("SECURITY: Multiple failed attempts tracked")
    void testValidateOtp_MultipleFailedAttempts() {
        // Given
        OtpStorageService.OtpData otpData = new OtpStorageService.OtpData(
                TEST_OTP,
                Instant.now().plusSeconds(300)
        );
        when(valueOperations.get("otp:" + TEST_PHONE)).thenReturn(otpData);
        when(valueOperations.get("verify_attempt:" + TEST_PHONE + ":" + TEST_IP)).thenReturn(null);
        when(valueOperations.setIfAbsent(anyString(), any(), any(Duration.class))).thenReturn(true);
        when(valueOperations.increment("otp_attempt:" + TEST_PHONE)).thenReturn(1L, 2L, 3L);
        when(valueOperations.increment("verify_attempt:" + TEST_PHONE + ":" + TEST_IP)).thenReturn(1L, 2L, 3L);

        // When - 3 failed attempts
        otpStorageService.validateAndConsumeOtp(TEST_PHONE, "wrong1", TEST_IP);
        otpStorageService.validateAndConsumeOtp(TEST_PHONE, "wrong2", TEST_IP);
        otpStorageService.validateAndConsumeOtp(TEST_PHONE, "wrong3", TEST_IP);

        // Then - Failed attempts should be tracked
        verify(valueOperations, atLeast(3)).increment("otp_attempt:" + TEST_PHONE);
        verify(valueOperations, atLeast(3)).increment("verify_attempt:" + TEST_PHONE + ":" + TEST_IP);
    }

    @Test
    @DisplayName("SECURITY: Account locked after max failed attempts")
    void testValidateOtp_AccountLockedAfterMaxAttempts() {
        // Given
        OtpStorageService.OtpData otpData = new OtpStorageService.OtpData(
                TEST_OTP,
                Instant.now().plusSeconds(300)
        );
        when(valueOperations.get("otp:" + TEST_PHONE)).thenReturn(otpData);
        when(valueOperations.get("verify_attempt:" + TEST_PHONE + ":" + TEST_IP)).thenReturn(null);
        when(valueOperations.setIfAbsent(anyString(), any(), any(Duration.class))).thenReturn(true);
        when(valueOperations.increment("otp_attempt:" + TEST_PHONE)).thenReturn(3L);
        when(valueOperations.increment("verify_attempt:" + TEST_PHONE + ":" + TEST_IP)).thenReturn(3L);

        // When - Failed attempt that reaches limit
        otpStorageService.validateAndConsumeOtp(TEST_PHONE, "wrong", TEST_IP);

        // Then - Rate limit should be set
        verify(valueOperations).set(
                eq("phone_rate_limit:" + TEST_PHONE),
                eq("rate_limited"),
                any(Duration.class)
        );
    }

    // ==================== SESSION MANAGEMENT TESTS ====================

    @Test
    @DisplayName("SUCCESS: State exists in Redis")
    void testStateExists_True() {
        // Given
        when(redisTemplate.hasKey("client_id:" + TEST_STATE)).thenReturn(true);

        // When
        boolean result = otpStorageService.stateExists(TEST_STATE);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("FAIL: State does not exist")
    void testStateExists_False() {
        // Given
        when(redisTemplate.hasKey("client_id:" + TEST_STATE)).thenReturn(false);

        // When
        boolean result = otpStorageService.stateExists(TEST_STATE);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("EDGE: State is null")
    void testStateExists_NullState() {
        // When
        boolean result = otpStorageService.stateExists(null);

        // Then
        assertThat(result).isFalse();
        verify(redisTemplate, never()).hasKey(anyString());
    }

    @Test
    @DisplayName("EDGE: State is empty")
    void testStateExists_EmptyState() {
        // When
        boolean result = otpStorageService.stateExists("");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("SUCCESS: Store OAuth2 parameters")
    void testStoreOAuth2Parameters_Success() {
        // Given
        String clientId = "test-client";
        String redirectUri = "http://localhost:8080/callback";
        String codeChallenge = "challenge123";
        String codeChallengeMethod = "S256";
        String scope = "read write";

        // When
        otpStorageService.storeOAuth2Parameters(
                clientId, TEST_STATE, redirectUri, codeChallenge, codeChallengeMethod, scope
        );

        // Then
        verify(valueOperations).set(eq("client_id:" + TEST_STATE), eq(clientId), eq(Duration.ofMinutes(10)));
        verify(valueOperations).set(eq("redirect_uri:" + TEST_STATE), eq(redirectUri), eq(Duration.ofMinutes(10)));
        verify(valueOperations).set(eq("code_challenge:" + TEST_STATE), eq(codeChallenge), eq(Duration.ofMinutes(10)));
        verify(valueOperations).set(eq("code_challenge_method:" + TEST_STATE), eq(codeChallengeMethod), eq(Duration.ofMinutes(10)));
        verify(valueOperations).set(eq("scope:" + TEST_STATE), eq(scope), eq(Duration.ofMinutes(10)));
    }

    @Test
    @DisplayName("SUCCESS: Mark state as consumed and cleanup")
    void testMarkStateAsConsumed_Success() {
        // When
        otpStorageService.markStateAsConsumed(TEST_STATE);

        // Then
        verify(redisTemplate).delete("client_id:" + TEST_STATE);
        verify(redisTemplate).delete("redirect_uri:" + TEST_STATE);
        verify(redisTemplate).delete("code_challenge:" + TEST_STATE);
        verify(redisTemplate).delete("code_challenge_method:" + TEST_STATE);
        verify(redisTemplate).delete("scope:" + TEST_STATE);
        verify(redisTemplate).delete("phone_number:" + TEST_STATE);
    }

    @Test
    @DisplayName("SUCCESS: Store and retrieve phone number")
    void testStoreAndGetPhoneNumber_Success() {
        // Given
        when(valueOperations.get("phone_number:" + TEST_STATE)).thenReturn(TEST_PHONE);

        // When
        otpStorageService.storePhoneNumber(TEST_STATE, TEST_PHONE);
        String result = otpStorageService.getPhoneNumber(TEST_STATE);

        // Then
        verify(valueOperations).set(
                eq("phone_number:" + TEST_STATE),
                eq(TEST_PHONE),
                eq(Duration.ofMinutes(15))
        );
        assertThat(result).isEqualTo(TEST_PHONE);
    }

    @Test
    @DisplayName("SUCCESS: Get session DTO")
    void testGetSessionDto_Success() {
        // Given
        String clientId = "test-client";
        String redirectUri = "http://localhost/callback";
        String codeChallenge = "challenge";
        String codeChallengeMethod = "S256";
        String scope = "read";

        when(valueOperations.get("client_id:" + TEST_STATE)).thenReturn(clientId);
        when(valueOperations.get("redirect_uri:" + TEST_STATE)).thenReturn(redirectUri);
        when(valueOperations.get("code_challenge:" + TEST_STATE)).thenReturn(codeChallenge);
        when(valueOperations.get("code_challenge_method:" + TEST_STATE)).thenReturn(codeChallengeMethod);
        when(valueOperations.get("scope:" + TEST_STATE)).thenReturn(scope);
        when(valueOperations.get("phone_number:" + TEST_STATE)).thenReturn(TEST_PHONE);

        // When
        OtpStorageService.SessionDto result = otpStorageService.getSessionDto(TEST_STATE);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.clientId()).isEqualTo(clientId);
        assertThat(result.redirectUri()).isEqualTo(redirectUri);
        assertThat(result.codeChallenge()).isEqualTo(codeChallenge);
        assertThat(result.codeChallengeMethod()).isEqualTo(codeChallengeMethod);
        assertThat(result.scope()).isEqualTo(scope);
        assertThat(result.phoneNumber()).isEqualTo(TEST_PHONE);
        assertThat(result.state()).isEqualTo(TEST_STATE);
    }

    // ==================== ATTACK SCENARIOS ====================

    @Test
    @DisplayName("SECURITY: SQL injection attempt in phone number")
    void testOtpOperations_SQLInjectionInPhone() {
        // Given
        String maliciousPhone = "' OR '1'='1";
        when(redisTemplate.hasKey("phone_rate_limit:" + maliciousPhone)).thenReturn(false);

        // When
        boolean result = otpStorageService.isPhoneNumberRateLimited(maliciousPhone);

        // Then - Should treat as normal string
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("SECURITY: Very long OTP code")
    void testValidateOtp_VeryLongOtp() {
        // Given
        String longOtp = "1".repeat(10000);
        OtpStorageService.OtpData otpData = new OtpStorageService.OtpData(
                TEST_OTP,
                Instant.now().plusSeconds(300)
        );
        when(valueOperations.get("otp:" + TEST_PHONE)).thenReturn(otpData);
        when(valueOperations.get("verify_attempt:" + TEST_PHONE + ":" + TEST_IP)).thenReturn(null);
        when(valueOperations.setIfAbsent(anyString(), any(), any(Duration.class))).thenReturn(true);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When
        boolean result = otpStorageService.validateAndConsumeOtp(TEST_PHONE, longOtp, TEST_IP);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("SECURITY: Replay attack - same OTP used twice")
    void testValidateOtp_ReplayAttack() {
        // Given
        OtpStorageService.OtpData otpData = new OtpStorageService.OtpData(
                TEST_OTP,
                Instant.now().plusSeconds(300)
        );
        when(valueOperations.get("otp:" + TEST_PHONE))
                .thenReturn(otpData)   // First attempt succeeds
                .thenReturn(null);     // Second attempt fails (OTP consumed)
        when(valueOperations.get("verify_attempt:" + TEST_PHONE + ":" + TEST_IP)).thenReturn(null);
        when(valueOperations.setIfAbsent(anyString(), any(), any(Duration.class))).thenReturn(true);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When - First use should succeed
        boolean firstAttempt = otpStorageService.validateAndConsumeOtp(TEST_PHONE, TEST_OTP, TEST_IP);

        // When - Second use should fail (replay attack)
        boolean secondAttempt = otpStorageService.validateAndConsumeOtp(TEST_PHONE, TEST_OTP, TEST_IP);

        // Then
        assertThat(firstAttempt).isTrue();
        assertThat(secondAttempt).isFalse();
    }
}
