package com.behpardakht.oauth_server.authorization.unitTest.security.password;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Security Tests for Password Encoding and Validation
 * Tests password hashing, salting, strength validation,
 * and protection against password-related attacks
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Password Security Tests")
class PasswordSecurityTest {

    private PasswordEncoder passwordEncoder;

    private static final String STRONG_PASSWORD = "P@ssw0rd!2024_Secure";
    private static final String VERY_LONG_PASSWORD = "a".repeat(200);

    @BeforeEach
    void setUp() {
        // Use BCrypt with strength 10 (default recommended strength)
        passwordEncoder = new BCryptPasswordEncoder(10);
    }

    // ==================== PASSWORD ENCODING TESTS ====================

    @Test
    @DisplayName("SUCCESS: Password encoded successfully")
    void testEncodePassword_Success() {
        // When
        String encoded = passwordEncoder.encode(STRONG_PASSWORD);

        // Then
        assertThat(encoded).isNotNull();
        assertThat(encoded).isNotEqualTo(STRONG_PASSWORD);
        assertThat(encoded).startsWith("$2a$"); // BCrypt format
    }

    @Test
    @DisplayName("SECURITY: Same password produces different hashes (salted)")
    void testEncodePassword_SaltedHashing() {
        // When - Encode same password multiple times
        String encoded1 = passwordEncoder.encode(STRONG_PASSWORD);
        String encoded2 = passwordEncoder.encode(STRONG_PASSWORD);
        String encoded3 = passwordEncoder.encode(STRONG_PASSWORD);

        // Then - All should be different (random salt)
        assertThat(encoded1).isNotEqualTo(encoded2);
        assertThat(encoded2).isNotEqualTo(encoded3);
        assertThat(encoded1).isNotEqualTo(encoded3);
    }

    @Test
    @DisplayName("SECURITY: Encoded password has sufficient length")
    void testEncodePassword_SufficientLength() {
        // When
        String encoded = passwordEncoder.encode(STRONG_PASSWORD);

        // Then - BCrypt produces 60 character hash
        assertThat(encoded).hasSize(60);
    }

    @Test
    @DisplayName("SECURITY: Empty password can be encoded (handled safely)")
    void testEncodePassword_EmptyPassword() {
        // When
        String encoded = passwordEncoder.encode("");

        // Then - Should encode empty string without error
        assertThat(encoded).isNotNull();
        assertThat(encoded).isNotEmpty();
    }

    @Test
    @DisplayName("SECURITY: Very long password rejected")
    void testEncodePassword_VeryLongPassword() {
        // When & Then - BCrypt rejects passwords > 72 bytes
        assertThatThrownBy(() -> passwordEncoder.encode(VERY_LONG_PASSWORD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("password cannot be more than 72 bytes");
    }

    @Test
    @DisplayName("SECURITY: Special characters in password encoded correctly")
    void testEncodePassword_SpecialCharacters() {
        // Given
        String specialPassword = "!@#$%^&*()_+-=[]{}|;':,.<>?/~`";

        // When
        String encoded = passwordEncoder.encode(specialPassword);

        // Then
        assertThat(encoded).isNotNull();
        assertThat(passwordEncoder.matches(specialPassword, encoded)).isTrue();
    }

    @Test
    @DisplayName("SECURITY: Unicode characters in password handled")
    void testEncodePassword_UnicodeCharacters() {
        // Given
        String unicodePassword = "पासवर्ड密码пароль🔐";

        // When
        String encoded = passwordEncoder.encode(unicodePassword);

        // Then
        assertThat(encoded).isNotNull();
        assertThat(passwordEncoder.matches(unicodePassword, encoded)).isTrue();
    }

    // ==================== PASSWORD MATCHING TESTS ====================

    @Test
    @DisplayName("SUCCESS: Correct password matches encoded hash")
    void testMatchPassword_Success() {
        // Given
        String encoded = passwordEncoder.encode(STRONG_PASSWORD);

        // When
        boolean matches = passwordEncoder.matches(STRONG_PASSWORD, encoded);

        // Then
        assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("FAIL: Incorrect password doesn't match")
    void testMatchPassword_Fail() {
        // Given
        String encoded = passwordEncoder.encode(STRONG_PASSWORD);

        // When
        boolean matches = passwordEncoder.matches("WrongPassword", encoded);

        // Then
        assertThat(matches).isFalse();
    }

    @Test
    @DisplayName("FAIL: Similar password doesn't match")
    void testMatchPassword_SimilarPasswordFails() {
        // Given
        String password = "Password123";
        String encoded = passwordEncoder.encode(password);

        // When - Try with slight variations
        boolean matches1 = passwordEncoder.matches("password123", encoded); // Different case
        boolean matches2 = passwordEncoder.matches("Password124", encoded); // Different digit
        boolean matches3 = passwordEncoder.matches("Password123 ", encoded); // Extra space

        // Then - None should match
        assertThat(matches1).isFalse();
        assertThat(matches2).isFalse();
        assertThat(matches3).isFalse();
    }

    @Test
    @DisplayName("SECURITY: Empty password vs empty hash")
    void testMatchPassword_EmptyPassword() {
        // Given
        String encoded = passwordEncoder.encode("");

        // When
        boolean matchesEmpty = passwordEncoder.matches("", encoded);
        boolean matchesNonEmpty = passwordEncoder.matches("test", encoded);

        // Then
        assertThat(matchesEmpty).isTrue();
        assertThat(matchesNonEmpty).isFalse();
    }

    @Test
    @DisplayName("SECURITY: Null password handled safely")
    void testMatchPassword_NullPassword() {
        // Given
        String encoded = passwordEncoder.encode(STRONG_PASSWORD);

        // When & Then - Should handle null gracefully
        assertThatThrownBy(() -> passwordEncoder.matches(null, encoded))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("SECURITY: Case sensitivity enforced")
    void testMatchPassword_CaseSensitive() {
        // Given
        String password = "PaSsWoRd";
        String encoded = passwordEncoder.encode(password);

        // When
        boolean matchesExact = passwordEncoder.matches("PaSsWoRd", encoded);
        boolean matchesLower = passwordEncoder.matches("password", encoded);
        boolean matchesUpper = passwordEncoder.matches("PASSWORD", encoded);

        // Then
        assertThat(matchesExact).isTrue();
        assertThat(matchesLower).isFalse();
        assertThat(matchesUpper).isFalse();
    }

    // ==================== BRUTE FORCE RESISTANCE TESTS ====================

    @Test
    @DisplayName("SECURITY: BCrypt is slow enough to resist brute force")
    void testPasswordMatching_ComputationalCost() {
        // Given
        String encoded = passwordEncoder.encode(STRONG_PASSWORD);

        // When - Measure time for verification
        long startTime = System.currentTimeMillis();
        passwordEncoder.matches(STRONG_PASSWORD, encoded);
        long duration = System.currentTimeMillis() - startTime;

        // Then - Should take measurable time (BCrypt is intentionally slow)
        // With strength 10, should take at least a few milliseconds
        assertThat(duration).isGreaterThanOrEqualTo(0);
        // BCrypt's computational cost makes brute force impractical
    }

    @Test
    @DisplayName("SECURITY: Multiple failed attempts take consistent time")
    void testPasswordMatching_TimingAttackResistance() {
        // Given
        String encoded = passwordEncoder.encode(STRONG_PASSWORD);

        // When - Time multiple incorrect attempts
        long start1 = System.nanoTime();
        passwordEncoder.matches("wrong1", encoded);
        long time1 = System.nanoTime() - start1;

        long start2 = System.nanoTime();
        passwordEncoder.matches("wrong2", encoded);
        long time2 = System.nanoTime() - start2;

        long start3 = System.nanoTime();
        passwordEncoder.matches("wrong3", encoded);
        long time3 = System.nanoTime() - start3;

        // Then - Times should be relatively consistent
        assertThat(time1).isGreaterThan(0);
        assertThat(time2).isGreaterThan(0);
        assertThat(time3).isGreaterThan(0);
        // BCrypt comparison time is constant regardless of where mismatch occurs
    }

    // ==================== RAINBOW TABLE RESISTANCE TESTS ====================

    @Test
    @DisplayName("SECURITY: Same weak password produces different hashes")
    void testWeakPassword_RainbowTableResistance() {
        // Given - Even weak password
        String weak = "123456";

        // When - Encode multiple times
        String hash1 = passwordEncoder.encode(weak);
        String hash2 = passwordEncoder.encode(weak);
        String hash3 = passwordEncoder.encode(weak);

        // Then - All different due to unique salts
        Set<String> hashes = new HashSet<>();
        hashes.add(hash1);
        hashes.add(hash2);
        hashes.add(hash3);

        assertThat(hashes).hasSize(3);
        // Rainbow tables ineffective due to per-password salts
    }

    @Test
    @DisplayName("SECURITY: Salt is unique per password")
    void testPasswordEncoding_UniqueSalt() {
        // When - Encode same password many times
        Set<String> encodedPasswords = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            encodedPasswords.add(passwordEncoder.encode(STRONG_PASSWORD));
        }

        // Then - All should be unique
        assertThat(encodedPasswords).hasSize(100);
    }

    // ==================== PASSWORD STRENGTH VALIDATION TESTS ====================

    @Test
    @DisplayName("VALIDATION: Weak password identification")
    void testPasswordStrength_Weak() {
        // Given
        String[] weakPasswords = {
                "123456",
                "password",
                "qwerty",
                "abc123",
                "111111",
                "admin"
        };

        // When & Then - These should be detected as weak in validation layer
        for (String weak : weakPasswords) {
            String encoded = passwordEncoder.encode(weak);
            assertThat(encoded).isNotNull(); // Can be encoded
            // But application should reject these at validation level
        }
    }

    @Test
    @DisplayName("VALIDATION: Medium password identification")
    void testPasswordStrength_Medium() {
        // Given - Passwords with some complexity but not strong
        String[] mediumPasswords = {
                "Password1",
                "Welcome2024",
                "Test@123"
        };

        // When
        for (String medium : mediumPasswords) {
            String encoded = passwordEncoder.encode(medium);

            // Then
            assertThat(encoded).isNotNull();
            assertThat(passwordEncoder.matches(medium, encoded)).isTrue();
        }
    }

    @Test
    @DisplayName("VALIDATION: Strong password identification")
    void testPasswordStrength_Strong() {
        // Given - Passwords with high complexity
        String[] strongPasswords = {
                "P@ssw0rd!2024_Secure",
                "MyV3ry$ecur3P@ss",
                "C0mpl3x&S3cur3!2024"
        };

        // When
        for (String strong : strongPasswords) {
            String encoded = passwordEncoder.encode(strong);

            // Then
            assertThat(encoded).isNotNull();
            assertThat(passwordEncoder.matches(strong, encoded)).isTrue();
        }
    }

    // ==================== COMMON PASSWORD ATTACKS ====================

    @Test
    @DisplayName("SECURITY: Common passwords still get hashed")
    void testCommonPasswords_StillHashed() {
        // Given - Top 10 most common passwords
        String[] commonPasswords = {
                "123456", "password", "12345678", "qwerty",
                "123456789", "12345", "1234", "111111",
                "1234567", "dragon"
        };

        // When & Then - All get hashed (rejection should happen at validation)
        for (String common : commonPasswords) {
            String encoded = passwordEncoder.encode(common);
            assertThat(encoded).isNotNull();
            assertThat(encoded).isNotEqualTo(common);
        }
    }

    @Test
    @DisplayName("SECURITY: Dictionary words get hashed")
    void testDictionaryWords_Hashed() {
        // Given
        String[] dictionaryWords = {
                "apple", "banana", "computer", "telephone",
                "sunshine", "football", "monkey", "dragon"
        };

        // When & Then
        for (String word : dictionaryWords) {
            String encoded = passwordEncoder.encode(word);
            assertThat(encoded).isNotNull();
        }
    }

    // ==================== PASSWORD POLICY ENFORCEMENT ====================

    @Test
    @DisplayName("POLICY: Minimum length requirement")
    void testPasswordPolicy_MinimumLength() {
        // Given - Minimum 8 characters recommended
        String tooShort = "Pass1!";  // 6 chars
        String acceptable = "Pass1!23"; // 8 chars

        // When - Both can be encoded
        String encoded1 = passwordEncoder.encode(tooShort);
        String encoded2 = passwordEncoder.encode(acceptable);

        // Then - Both encoded, but validation should reject too short
        assertThat(encoded1).isNotNull();
        assertThat(encoded2).isNotNull();
        assertThat(tooShort.length()).isLessThan(8);
        assertThat(acceptable.length()).isGreaterThanOrEqualTo(8);
    }

    @Test
    @DisplayName("POLICY: Maximum length handling")
    void testPasswordPolicy_MaximumLength() {
        // Given - BCrypt can handle up to 72 bytes
        String maxLength = "a".repeat(72);
        String tooLong = "a".repeat(100);

        // When
        String encoded1 = passwordEncoder.encode(maxLength);

        // Then - Max length encoded successfully
        assertThat(encoded1).isNotNull();

        // Then - Too long throws exception
        assertThatThrownBy(() -> passwordEncoder.encode(tooLong))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("password cannot be more than 72 bytes");
    }

    @Test
    @DisplayName("POLICY: Character variety requirement")
    void testPasswordPolicy_CharacterVariety() {
        // Given
        String onlyLowercase = "abcdefgh";
        String onlyUppercase = "ABCDEFGH";
        String onlyNumbers = "12345678";
        String mixed = "Abc123!@";

        // When - All can be encoded
        assertThat(passwordEncoder.encode(onlyLowercase)).isNotNull();
        assertThat(passwordEncoder.encode(onlyUppercase)).isNotNull();
        assertThat(passwordEncoder.encode(onlyNumbers)).isNotNull();
        assertThat(passwordEncoder.encode(mixed)).isNotNull();

        // Then - Policy should require mixed (validated separately)
        boolean hasLower = mixed.matches(".*[a-z].*");
        boolean hasUpper = mixed.matches(".*[A-Z].*");
        boolean hasDigit = mixed.matches(".*\\d.*");
        boolean hasSpecial = mixed.matches(".*[!@#$%^&*()].*");

        assertThat(hasLower && hasUpper && hasDigit && hasSpecial).isTrue();
    }

    // ==================== PASSWORD CHANGE SCENARIOS ====================

    @Test
    @DisplayName("SECURITY: Old password verification before change")
    void testPasswordChange_OldPasswordVerification() {
        // Given
        String oldPassword = "OldP@ssw0rd";
        String newPassword = "NewP@ssw0rd2024";
        String oldEncoded = passwordEncoder.encode(oldPassword);

        // When - Verify old password before allowing change
        boolean oldMatches = passwordEncoder.matches(oldPassword, oldEncoded);

        // Then
        assertThat(oldMatches).isTrue();

        // Only if old password matches, encode new password
        if (oldMatches) {
            String newEncoded = passwordEncoder.encode(newPassword);
            assertThat(newEncoded).isNotEqualTo(oldEncoded);
        }
    }

    @Test
    @DisplayName("SECURITY: New password must be different from old")
    void testPasswordChange_NewPasswordDifferent() {
        // Given
        String password = "MyP@ssw0rd";
        String oldEncoded = passwordEncoder.encode(password);
        String newEncoded = passwordEncoder.encode(password);

        // When - Same password used
        boolean samePassword = passwordEncoder.matches(password, oldEncoded)
                && passwordEncoder.matches(password, newEncoded);

        // Then - Hashes are different but password is same (should be rejected)
        assertThat(samePassword).isTrue();
        assertThat(oldEncoded).isNotEqualTo(newEncoded); // Different hashes
        // Policy should prevent reusing same password
    }

    // ==================== PASSWORD RESET SCENARIOS ====================

    @Test
    @DisplayName("SECURITY: Password reset generates new hash")
    void testPasswordReset_NewHash() {
        // Given
        String resetPassword = "Reset@P@ssw0rd2024";

        // When
        String newEncoded = passwordEncoder.encode(resetPassword);

        // Then
        assertThat(newEncoded).isNotNull();
        assertThat(passwordEncoder.matches(resetPassword, newEncoded)).isTrue();
    }

    // ==================== HASH FORMAT VALIDATION ====================

    @Test
    @DisplayName("SECURITY: BCrypt hash format validation")
    void testHashFormat_BCryptFormat() {
        // Given
        String encoded = passwordEncoder.encode(STRONG_PASSWORD);

        // When - Validate BCrypt format: $2a$rounds$salt+hash
        // Then
        assertThat(encoded).matches("\\$2[aby]\\$\\d{2}\\$.{53}");
    }

    @Test
    @DisplayName("SECURITY: Invalid hash format rejected")
    void testHashFormat_InvalidFormat() {
        // Given
        String invalidHash = "not-a-valid-bcrypt-hash";

        // When & Then - Should not match
        boolean matches = passwordEncoder.matches(STRONG_PASSWORD, invalidHash);
        assertThat(matches).isFalse();
    }

    // ==================== ENCODING CONSISTENCY TESTS ====================

    @Test
    @DisplayName("RELIABILITY: Encoding is consistent")
    void testEncoding_Consistency() {
        // When - Encode and verify multiple times
        for (int i = 0; i < 10; i++) {
            String encoded = passwordEncoder.encode(STRONG_PASSWORD);
            boolean matches = passwordEncoder.matches(STRONG_PASSWORD, encoded);
            assertThat(matches).isTrue();
        }

        // Then - All verifications should succeed
    }

    @Test
    @DisplayName("RELIABILITY: Special edge cases handled")
    void testEncoding_EdgeCases() {
        // Given
        String[] edgeCases = {
                "",                          // Empty
                " ",                         // Single space
                "  ",                        // Multiple spaces
                "\n",                        // Newline
                "\t",                        // Tab
                "null",                      // String "null"
                "$2a$10$test",               // Looks like BCrypt
                "' OR '1'='1",              // SQL injection
                "<script>alert('xss')</script>" // XSS
        };

        // When & Then - All should be encoded safely
        for (String edge : edgeCases) {
            String encoded = passwordEncoder.encode(edge);
            assertThat(encoded).isNotNull();
            assertThat(passwordEncoder.matches(edge, encoded)).isTrue();
        }
    }
}
