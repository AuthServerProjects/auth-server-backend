package com.behpardakht.oauth_server.authorization.unitTest.security.jwt;

import com.behpardakht.oauth_server.authorization.security.resourceServer.CustomJwtAuthenticationToken;
import com.behpardakht.oauth_server.authorization.security.resourceServer.JwtAuthenticationConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Security Tests for JWT Authentication and Token Conversion
 * Tests JWT parsing, claims extraction, authority conversion,
 * and protection against JWT-based attacks
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Authentication Security Tests")
class JwtAuthenticationSecurityTest {

    @InjectMocks
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    private Jwt.Builder jwtBuilder;
    private static final String CLIENT_ID = "test-client";
    private static final String ISSUER = "https://auth.example.com";
    private static final String SUBJECT = "user123";

    @BeforeEach
    void setUp() {
        jwtBuilder = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .header("typ", "JWT")
                .issuer(ISSUER)
                .subject(SUBJECT)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claim("client-id", CLIENT_ID);
    }

    // ==================== SUCCESS SCENARIOS ====================

    @Test
    @DisplayName("SUCCESS: JWT with roles and scopes converted correctly")
    void testConvertJwt_WithRolesAndScopes_Success() {
        // Given
        Jwt jwt = jwtBuilder
                .claim("roles", List.of("ADMIN", "USER"))
                .claim("scope", List.of("read", "write"))
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAuthorities()).hasSize(4);
        assertThat(result.getAuthorities())
                .extracting("authority")
                .contains("ROLE_ADMIN", "ROLE_USER", "SCOPE_read", "SCOPE_write");
        assertThat(result.getClientId()).isEqualTo(CLIENT_ID);
    }

    @Test
    @DisplayName("SUCCESS: JWT with only roles")
    void testConvertJwt_RolesOnly() {
        // Given
        Jwt jwt = jwtBuilder
                .claim("roles", List.of("USER"))
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("SUCCESS: JWT with only scopes")
    void testConvertJwt_ScopesOnly() {
        // Given
        Jwt jwt = jwtBuilder
                .claim("scope", List.of("read", "write", "delete"))
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAuthorities()).hasSize(3);
        assertThat(result.getAuthorities())
                .extracting("authority")
                .contains("SCOPE_read", "SCOPE_write", "SCOPE_delete");
    }

    @Test
    @DisplayName("SUCCESS: JWT without roles or scopes")
    void testConvertJwt_NoRolesOrScopes() {
        // Given
        Jwt jwt = jwtBuilder.build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAuthorities()).isEmpty();
        assertThat(result.getClientId()).isEqualTo(CLIENT_ID);
    }

    // ==================== ROLE CONVERSION TESTS ====================

    @Test
    @DisplayName("SUCCESS: Role prefix added automatically")
    void testConvertJwt_RolePrefixAdded() {
        // Given
        Jwt jwt = jwtBuilder
                .claim("roles", List.of("ADMIN"))
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then
        Assertions.assertNotNull(result);
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("SUCCESS: Multiple roles processed correctly")
    void testConvertJwt_MultipleRoles() {
        // Given
        Jwt jwt = jwtBuilder
                .claim("roles", List.of("ADMIN", "USER", "MODERATOR", "SUPER_ADMIN"))
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then
        Assertions.assertNotNull(result);
        assertThat(result.getAuthorities()).hasSize(4);
        assertThat(result.getAuthorities())
                .extracting("authority")
                .contains("ROLE_ADMIN", "ROLE_USER", "ROLE_MODERATOR", "ROLE_SUPER_ADMIN");
    }

    @Test
    @DisplayName("EDGE: Empty roles list handled")
    void testConvertJwt_EmptyRolesList() {
        // Given
        Jwt jwt = jwtBuilder
                .claim("roles", List.of())
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then
        Assertions.assertNotNull(result);
        assertThat(result.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("SECURITY: Null roles filtered out")
    void testConvertJwt_NullRolesFiltered() {
        // Given - Roles list containing null
        List<String> rolesWithNull = new ArrayList<>();
        rolesWithNull.add("ADMIN");
        rolesWithNull.add(null);
        rolesWithNull.add("USER");
        
        Jwt jwt = jwtBuilder
                .claim("roles", rolesWithNull)
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then - Null should be filtered
        Assertions.assertNotNull(result);
        assertThat(result.getAuthorities()).hasSize(2);
        assertThat(result.getAuthorities())
                .extracting("authority")
                .contains("ROLE_ADMIN", "ROLE_USER")
                .doesNotContain("ROLE_null");
    }

    // ==================== SCOPE CONVERSION TESTS ====================

    @Test
    @DisplayName("SUCCESS: Scope prefix added automatically")
    void testConvertJwt_ScopePrefixAdded() {
        // Given
        Jwt jwt = jwtBuilder
                .claim("scope", List.of("read"))
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then
        Assertions.assertNotNull(result);
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("SCOPE_read");
    }

    @Test
    @DisplayName("SUCCESS: Multiple scopes processed correctly")
    void testConvertJwt_MultipleScopes() {
        // Given
        Jwt jwt = jwtBuilder
                .claim("scope", List.of("read", "write", "delete", "admin"))
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then
        Assertions.assertNotNull(result);
        assertThat(result.getAuthorities()).hasSize(4);
        assertThat(result.getAuthorities())
                .extracting("authority")
                .contains("SCOPE_read", "SCOPE_write", "SCOPE_delete", "SCOPE_admin");
    }

    @Test
    @DisplayName("EDGE: Empty scopes list handled")
    void testConvertJwt_EmptyScopesList() {
        // Given
        Jwt jwt = jwtBuilder
                .claim("scope", List.of())
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then
        Assertions.assertNotNull(result);
        assertThat(result.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("SECURITY: Null scopes filtered out")
    void testConvertJwt_NullScopesFiltered() {
        // Given
        List<String> scopesWithNull = new ArrayList<>();
        scopesWithNull.add("read");
        scopesWithNull.add(null);
        scopesWithNull.add("write");
        
        Jwt jwt = jwtBuilder
                .claim("scope", scopesWithNull)
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then
        Assertions.assertNotNull(result);
        assertThat(result.getAuthorities()).hasSize(2);
        assertThat(result.getAuthorities())
                .extracting("authority")
                .contains("SCOPE_read", "SCOPE_write");
    }

    // ==================== CLIENT ID EXTRACTION TESTS ====================

    @Test
    @DisplayName("SUCCESS: Client ID extracted correctly")
    void testConvertJwt_ClientIdExtracted() {
        // Given
        Jwt jwt = jwtBuilder
                .claim("client-id", "special-client-123")
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then
        Assertions.assertNotNull(result);
        assertThat(result.getClientId()).isEqualTo("special-client-123");
    }

    @Test
    @DisplayName("EDGE: Missing client-id returns 'null' string")
    void testConvertJwt_MissingClientId() {
        // Given - No client-id claim
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .issuer(ISSUER)
                .subject(SUBJECT)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then - Returns string "null"
        Assertions.assertNotNull(result);
        assertThat(result.getClientId()).isEqualTo("null");
    }

    @Test
    @DisplayName("EDGE: Client ID with special characters")
    void testConvertJwt_ClientIdSpecialCharacters() {
        // Given
        String specialClientId = "client-123_test@domain.com";
        Jwt jwt = jwtBuilder
                .claim("client-id", specialClientId)
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then
        Assertions.assertNotNull(result);
        assertThat(result.getClientId()).isEqualTo(specialClientId);
    }

    // ==================== JWT CLAIMS ATTACK SCENARIOS ====================

    @Test
    @DisplayName("SECURITY: Malicious role injection attempt")
    void testConvertJwt_MaliciousRoleInjection() {
        // Given - Attacker tries to inject admin role through claims
        Jwt jwt = jwtBuilder
                .claim("roles", List.of("USER", "SUPER_ADMIN", "'; DROP TABLE users; --"))
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then - All roles processed as strings (no code execution)
        Assertions.assertNotNull(result);
        assertThat(result.getAuthorities()).hasSize(3);
        assertThat(result.getAuthorities())
                .extracting("authority")
                .contains("ROLE_USER", "ROLE_SUPER_ADMIN", "ROLE_'; DROP TABLE users; --");
        // Authorization layer should validate against allowed roles
    }

    @Test
    @DisplayName("SECURITY: XSS in role claim")
    void testConvertJwt_XSSInRole() {
        // Given
        Jwt jwt = jwtBuilder
                .claim("roles", List.of("<script>alert('xss')</script>"))
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then - XSS content treated as literal string
        Assertions.assertNotNull(result);
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority())
                .isEqualTo("ROLE_<script>alert('xss')</script>");
    }

    @Test
    @DisplayName("SECURITY: Very long role name (DoS prevention)")
    void testConvertJwt_VeryLongRoleName() {
        // Given
        String longRole = "A".repeat(10000);
        Jwt jwt = jwtBuilder
                .claim("roles", List.of(longRole))
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then - Should handle without crashing
        Assertions.assertNotNull(result);
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority())
                .startsWith("ROLE_A");
    }

    @Test
    @DisplayName("SECURITY: Duplicate roles in JWT")
    void testConvertJwt_DuplicateRoles() {
        // Given
        Jwt jwt = jwtBuilder
                .claim("roles", List.of("ADMIN", "USER", "ADMIN", "USER"))
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then - Duplicates are included (Set conversion happens elsewhere if needed)
        Assertions.assertNotNull(result);
        assertThat(result.getAuthorities()).hasSize(4);
    }

    @Test
    @DisplayName("SECURITY: Case-sensitive role matching")
    void testConvertJwt_RoleCaseSensitivity() {
        // Given
        Jwt jwt = jwtBuilder
                .claim("roles", List.of("Admin", "ADMIN", "admin"))
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then - All treated as different roles (case-sensitive)
        Assertions.assertNotNull(result);
        assertThat(result.getAuthorities()).hasSize(3);
        assertThat(result.getAuthorities())
                .extracting("authority")
                .contains("ROLE_Admin", "ROLE_ADMIN", "ROLE_admin");
    }

    // ==================== JWT STANDARD CLAIMS TESTS ====================

    @Test
    @DisplayName("SECURITY: JWT with all standard claims")
    void testConvertJwt_AllStandardClaims() {
        // Given
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .issuer(ISSUER)
                .subject("user@example.com")
                .audience(List.of("api.example.com"))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .notBefore(now)
                .jti("jwt-id-123")
                .claim("client-id", CLIENT_ID)
                .claim("roles", List.of("USER"))
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken().getSubject()).isEqualTo("user@example.com");
        assertThat(result.getToken().getIssuer().toString()).isEqualTo(ISSUER);
    }

    @Test
    @DisplayName("SECURITY: Expired JWT processed (validation happens elsewhere)")
    void testConvertJwt_ExpiredToken() {
        // Given - Expired token
        Instant now = Instant.now();
        Jwt jwt = jwtBuilder
                .issuedAt(now.minusSeconds(7200)) // Issued 2 hours ago
                .expiresAt(now.minusSeconds(3600)) // Expired 1 hour ago
                .claim("roles", List.of("USER"))
                .build();

        // When - Converter doesn't validate expiration
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then - Conversion succeeds (validation is separate concern)
        assertThat(result).isNotNull();
        // JWT validation should happen in JwtDecoder, not converter
    }

    @Test
    @DisplayName("SECURITY: JWT not yet valid (nbf claim)")
    void testConvertJwt_NotYetValid() {
        // Given - Not valid yet
        Jwt jwt = jwtBuilder
                .notBefore(Instant.now().plusSeconds(3600)) // Valid in 1 hour
                .claim("roles", List.of("USER"))
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then - Conversion succeeds (validation is separate)
        assertThat(result).isNotNull();
    }

    // ==================== AUTHORITY COMBINATION TESTS ====================

    @Test
    @DisplayName("SUCCESS: Roles and scopes combined correctly")
    void testConvertJwt_RolesAndScopesCombined() {
        // Given
        Jwt jwt = jwtBuilder
                .claim("roles", List.of("ADMIN", "USER"))
                .claim("scope", List.of("read", "write"))
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then - Roles processed first, then scopes
        Assertions.assertNotNull(result);
        List<String> authorities = result.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        
        assertThat(authorities).hasSize(4);
        // Check roles come before scopes in the stream
        assertThat(authorities.subList(0, 2)).contains("ROLE_ADMIN", "ROLE_USER");
        assertThat(authorities.subList(2, 4)).contains("SCOPE_read", "SCOPE_write");
    }

    @Test
    @DisplayName("SUCCESS: Large number of authorities handled")
    void testConvertJwt_LargeNumberOfAuthorities() {
        // Given - Many roles and scopes
        List<String> manyRoles = new ArrayList<>();
        List<String> manyScopes = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            manyRoles.add("ROLE_" + i);
            manyScopes.add("scope_" + i);
        }
        
        Jwt jwt = jwtBuilder
                .claim("roles", manyRoles)
                .claim("scope", manyScopes)
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then - Should handle efficiently
        Assertions.assertNotNull(result);
        assertThat(result.getAuthorities()).hasSize(100);
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("EDGE: Roles claim is wrong type")
    void testConvertJwt_RolesWrongType() {
        // Given - Roles as string instead of list
        Jwt jwt = jwtBuilder
                .claim("roles", "ADMIN") // Wrong type
                .build();

        // When & Then - Should handle ClassCastException
        assertThatThrownBy(() -> jwtAuthenticationConverter.convert(jwt))
                .isInstanceOf(ClassCastException.class);
    }

    @Test
    @DisplayName("EDGE: Scopes claim is wrong type")
    void testConvertJwt_ScopesWrongType() {
        // Given
        Jwt jwt = jwtBuilder
                .claim("scope", "read") // Wrong type
                .build();

        // When & Then
        assertThatThrownBy(() -> jwtAuthenticationConverter.convert(jwt))
                .isInstanceOf(ClassCastException.class);
    }

    @Test
    @DisplayName("EDGE: Claims map is empty")
    void testConvertJwt_EmptyClaims() {
        // Given - Minimal JWT
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        // When
        CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAuthorities()).isEmpty();
        assertThat(result.getClientId()).isEqualTo("null");
    }

    // ==================== CONCURRENT ACCESS TESTS ====================

    @Test
    @DisplayName("RELIABILITY: Concurrent JWT conversion")
    void testConvertJwt_ConcurrentAccess() throws InterruptedException {
        // Given
        Jwt jwt = jwtBuilder
                .claim("roles", List.of("USER"))
                .claim("scope", List.of("read"))
                .build();

        // When - Convert same JWT from multiple threads
        Thread thread1 = new Thread(() -> {
            CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);
            assertThat(result).isNotNull();
        });
        
        Thread thread2 = new Thread(() -> {
            CustomJwtAuthenticationToken result = jwtAuthenticationConverter.convert(jwt);
            assertThat(result).isNotNull();
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // Then - Both should succeed
    }

    // ==================== NULL HANDLING TESTS ====================

    @Test
    @DisplayName("FAIL: Null JWT throws exception")
    void testConvertJwt_NullJwt() {
        // When & Then
        assertThatThrownBy(() -> jwtAuthenticationConverter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }
}
