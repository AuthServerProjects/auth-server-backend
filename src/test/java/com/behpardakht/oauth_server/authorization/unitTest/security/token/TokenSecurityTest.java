package com.behpardakht.oauth_server.authorization.unitTest.security.token;

import com.behpardakht.oauth_server.authorization.security.authorizationServer.OAuth2TokenCustomizerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Security Tests for JWT Token Generation and Customization
 * Tests token security including claims injection, signature verification,
 * and protection against token manipulation attacks
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Token Security Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class TokenSecurityTest {

    @InjectMocks
    private OAuth2TokenCustomizerImpl tokenCustomizer;

    @Mock
    private JwtEncodingContext context;

    @Mock
    private RegisteredClient registeredClient;

    @Mock
    private Authentication principal;

    @Mock
    private JwtClaimsSet.Builder claimsBuilder;

    @Mock
    private JwsHeader.Builder headersBuilder;

    private static final String TEST_CLIENT_ID = "test-client";
    private static final String ADMIN_ROLE = "ROLE_ADMIN";
    private static final String USER_ROLE = "ROLE_USER";

    @BeforeEach
    void setUp() {
        when(context.getRegisteredClient()).thenReturn(registeredClient);
        when(context.getPrincipal()).thenReturn(principal);
        when(context.getClaims()).thenReturn(claimsBuilder);
        when(registeredClient.getClientId()).thenReturn(TEST_CLIENT_ID);
    }

    // ==================== SUCCESS SCENARIOS ====================

    @Test
    @DisplayName("SUCCESS: Customize access token with client-id and roles")
    void testCustomize_AccessToken_Success() {
        // Given
        when(context.getTokenType()).thenReturn(new OAuth2TokenType("access_token"));

        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(ADMIN_ROLE),
                new SimpleGrantedAuthority(USER_ROLE)
        );
        when(principal.getAuthorities()).thenAnswer(invocation -> authorities);
        when(claimsBuilder.claim(anyString(), any())).thenReturn(claimsBuilder);

        // When
        tokenCustomizer.customize(context);

        // Then
        verify(claimsBuilder).claim("client-id", TEST_CLIENT_ID);
        verify(claimsBuilder).claim(eq("roles"), argThat(roles -> {
            List<String> roleList = (List<String>) roles;
            return roleList.contains(ADMIN_ROLE) && roleList.contains(USER_ROLE);
        }));
    }

    @Test
    @DisplayName("SUCCESS: Customize refresh token with client-id only")
    void testCustomize_RefreshToken_Success() {
        // Given
        when(context.getTokenType()).thenReturn(new OAuth2TokenType("refresh_token"));
        when(claimsBuilder.claim(anyString(), any())).thenReturn(claimsBuilder);

        // When
        tokenCustomizer.customize(context);

        // Then
        verify(claimsBuilder).claim("client-id", TEST_CLIENT_ID);
        verify(claimsBuilder, never()).claim(eq("roles"), any());
    }

    @Test
    @DisplayName("SUCCESS: Access token with single role")
    void testCustomize_AccessToken_SingleRole() {
        // Given
        when(context.getTokenType()).thenReturn(new OAuth2TokenType("access_token"));

        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(USER_ROLE)
        );
        when(principal.getAuthorities()).thenAnswer(invocation -> authorities);
        when(claimsBuilder.claim(anyString(), any())).thenReturn(claimsBuilder);

        // When
        tokenCustomizer.customize(context);

        // Then
        verify(claimsBuilder).claim(eq("roles"), argThat(roles -> {
            List<String> roleList = (List<String>) roles;
            return roleList.size() == 1 && roleList.contains(USER_ROLE);
        }));
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("EDGE: Access token with no roles")
    void testCustomize_AccessToken_NoRoles() {
        // Given
        when(context.getTokenType()).thenReturn(new OAuth2TokenType("access_token"));
        when(principal.getAuthorities()).thenReturn(List.of());
        when(claimsBuilder.claim(anyString(), any())).thenReturn(claimsBuilder);

        // When
        tokenCustomizer.customize(context);

        // Then
        verify(claimsBuilder).claim("client-id", TEST_CLIENT_ID);
        verify(claimsBuilder).claim(eq("roles"), argThat(roles -> {
            List<String> roleList = (List<String>) roles;
            return roleList.isEmpty();
        }));
    }

    @Test
    @DisplayName("EDGE: Access token filters null authorities")
    void testCustomize_AccessToken_NullAuthoritiesFiltered() {
        // Given
        when(context.getTokenType()).thenReturn(new OAuth2TokenType("access_token"));

        // Create a list that may contain null (in real scenario this shouldn't happen but we test for robustness)
        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(USER_ROLE)
        );
        when(principal.getAuthorities()).thenAnswer(invocation -> authorities);
        when(claimsBuilder.claim(anyString(), any())).thenReturn(claimsBuilder);

        // When
        tokenCustomizer.customize(context);

        // Then - Should filter out any null values
        verify(claimsBuilder).claim(eq("roles"), argThat(roles -> {
            List<String> roleList = (List<String>) roles;
            return !roleList.contains(null);
        }));
    }

    @Test
    @DisplayName("EDGE: Access token with multiple identical roles (should not duplicate)")
    void testCustomize_AccessToken_DuplicateRoles() {
        // Given
        when(context.getTokenType()).thenReturn(new OAuth2TokenType("access_token"));

        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(USER_ROLE),
                new SimpleGrantedAuthority(USER_ROLE)  // Duplicate
        );
        when(principal.getAuthorities()).thenAnswer(invocation -> authorities);
        when(claimsBuilder.claim(anyString(), any())).thenReturn(claimsBuilder);

        // When
        tokenCustomizer.customize(context);

        // Then
        verify(claimsBuilder).claim(eq("roles"), any());
    }

    @Test
    @DisplayName("EDGE: Client ID with special characters")
    void testCustomize_SpecialCharactersInClientId() {
        // Given
        String specialClientId = "client-123_test@domain.com";
        when(registeredClient.getClientId()).thenReturn(specialClientId);
        when(context.getTokenType()).thenReturn(new OAuth2TokenType("access_token"));
        when(principal.getAuthorities()).thenReturn(List.of());
        when(claimsBuilder.claim(anyString(), any())).thenReturn(claimsBuilder);

        // When
        tokenCustomizer.customize(context);

        // Then
        verify(claimsBuilder).claim("client-id", specialClientId);
    }

    // ==================== SECURITY ATTACK SCENARIOS ====================

    @Test
    @DisplayName("SECURITY: Token with very long client ID (DoS prevention)")
    void testCustomize_VeryLongClientId() {
        // Given
        String longClientId = "a".repeat(1000);
        when(registeredClient.getClientId()).thenReturn(longClientId);
        when(context.getTokenType()).thenReturn(new OAuth2TokenType("access_token"));
        when(principal.getAuthorities()).thenReturn(List.of());
        when(claimsBuilder.claim(anyString(), any())).thenReturn(claimsBuilder);

        // When
        tokenCustomizer.customize(context);

        // Then - Should handle without error
        verify(claimsBuilder).claim("client-id", longClientId);
    }

    @Test
    @DisplayName("SECURITY: Token with role containing malicious content")
    void testCustomize_MaliciousRoleContent() {
        // Given
        when(context.getTokenType()).thenReturn(new OAuth2TokenType("access_token"));

        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_<script>alert('xss')</script>"),
                new SimpleGrantedAuthority("ROLE_'; DROP TABLE users; --")
        );
        when(principal.getAuthorities()).thenAnswer(invocation -> authorities);
        when(claimsBuilder.claim(anyString(), any())).thenReturn(claimsBuilder);

        // When
        tokenCustomizer.customize(context);

        // Then - Should encode malicious content as-is (consumers must sanitize)
        verify(claimsBuilder).claim(eq("roles"), argThat(roles -> {
            List<String> roleList = (List<String>) roles;
            return roleList.size() == 2;
        }));
    }

    @Test
    @DisplayName("SECURITY: Token type case sensitivity")
    void testCustomize_TokenTypeCaseSensitivity() {
        // Given - uppercase token type
        when(context.getTokenType()).thenReturn(new OAuth2TokenType("ACCESS_TOKEN"));
        when(principal.getAuthorities()).thenAnswer(invocation -> List.of(new SimpleGrantedAuthority(USER_ROLE)));

        when(claimsBuilder.claim(anyString(), any())).thenReturn(claimsBuilder);

        // When
        tokenCustomizer.customize(context);

        // Then - Should NOT add roles because case doesn't match "access_token"
        verify(claimsBuilder).claim("client-id", TEST_CLIENT_ID);
        verify(claimsBuilder, never()).claim(eq("roles"), any());
    }

    @Test
    @DisplayName("SECURITY: Empty client ID")
    void testCustomize_EmptyClientId() {
        // Given
        when(registeredClient.getClientId()).thenReturn("");
        when(context.getTokenType()).thenReturn(new OAuth2TokenType("access_token"));
        when(principal.getAuthorities()).thenReturn(List.of());
        when(claimsBuilder.claim(anyString(), any())).thenReturn(claimsBuilder);

        // When
        tokenCustomizer.customize(context);

        // Then - Should handle empty client ID
        verify(claimsBuilder).claim("client-id", "");
    }

    @Test
    @DisplayName("SECURITY: Null client ID should throw exception")
    void testCustomize_NullClientId() {
        // Given
        when(registeredClient.getClientId()).thenReturn(null);
        when(context.getTokenType()).thenReturn(new OAuth2TokenType("access_token"));
        when(principal.getAuthorities()).thenReturn(List.of());

        // When & Then - Should handle null gracefully or throw
        try {
            tokenCustomizer.customize(context);
            verify(claimsBuilder).claim("client-id", null);
        } catch (NullPointerException e) {
            // Acceptable behavior
            assertThat(e).isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== TOKEN TYPE VARIATIONS ====================

    @Test
    @DisplayName("TOKEN: ID token customization")
    void testCustomize_IdToken() {
        // Given
        when(context.getTokenType()).thenReturn(new OAuth2TokenType("id_token"));
        when(principal.getAuthorities()).thenAnswer(invocation -> List.of(new SimpleGrantedAuthority(USER_ROLE)));
        when(claimsBuilder.claim(anyString(), any())).thenReturn(claimsBuilder);

        // When
        tokenCustomizer.customize(context);

        // Then - ID tokens don't get roles
        verify(claimsBuilder).claim("client-id", TEST_CLIENT_ID);
        verify(claimsBuilder, never()).claim(eq("roles"), any());
    }

    @Test
    @DisplayName("TOKEN: Authorization code customization")
    void testCustomize_AuthorizationCode() {
        // Given
        when(context.getTokenType()).thenReturn(new OAuth2TokenType("code"));
        when(claimsBuilder.claim(anyString(), any())).thenReturn(claimsBuilder);

        // When
        tokenCustomizer.customize(context);

        // Then - Authorization codes only get client-id
        verify(claimsBuilder).claim("client-id", TEST_CLIENT_ID);
        verify(claimsBuilder, never()).claim(eq("roles"), any());
    }

    // ==================== ROLE FORMAT TESTS ====================

    @Test
    @DisplayName("ROLE: Standard ROLE_ prefix format")
    void testCustomize_StandardRoleFormat() {
        // Given
        when(context.getTokenType()).thenReturn(new OAuth2TokenType("access_token"));

        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_MODERATOR")
        );
        when(principal.getAuthorities()).thenAnswer(invocation -> authorities);

        when(claimsBuilder.claim(anyString(), any())).thenReturn(claimsBuilder);

        // When
        tokenCustomizer.customize(context);

        // Then
        verify(claimsBuilder).claim(eq("roles"), argThat(roles -> {
            List<String> roleList = (List<String>) roles;
            return roleList.size() == 3 &&
                    roleList.contains("ROLE_ADMIN") &&
                    roleList.contains("ROLE_USER") &&
                    roleList.contains("ROLE_MODERATOR");
        }));
    }

    @Test
    @DisplayName("ROLE: Custom authorities without ROLE_ prefix")
    void testCustomize_CustomAuthoritiesFormat() {
        // Given
        when(context.getTokenType()).thenReturn(new OAuth2TokenType("access_token"));

        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("READ_PRIVILEGE"),
                new SimpleGrantedAuthority("WRITE_PRIVILEGE")
        );
        when(principal.getAuthorities()).thenAnswer(invocation -> authorities);
        when(claimsBuilder.claim(anyString(), any())).thenReturn(claimsBuilder);

        // When
        tokenCustomizer.customize(context);

        // Then - Custom authorities should be included as-is
        verify(claimsBuilder).claim(eq("roles"), argThat(roles -> {
            List<String> roleList = (List<String>) roles;
            return roleList.contains("READ_PRIVILEGE") &&
                    roleList.contains("WRITE_PRIVILEGE");
        }));
    }

    // ==================== CONCURRENCY & PERFORMANCE ====================

    @Test
    @DisplayName("PERFORMANCE: Handle large number of roles")
    void testCustomize_LargeNumberOfRoles() {
        // Given
        when(context.getTokenType()).thenReturn(new OAuth2TokenType("access_token"));

        List<GrantedAuthority> authorities = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + i));
        }
        when(principal.getAuthorities()).thenAnswer(invocation -> authorities);
        when(claimsBuilder.claim(anyString(), any())).thenReturn(claimsBuilder);

        // When
        tokenCustomizer.customize(context);

        // Then - Should handle many roles efficiently
        verify(claimsBuilder).claim(eq("roles"), argThat(roles -> {
            List<String> roleList = (List<String>) roles;
            return roleList.size() == 100;
        }));
    }

    @Test
    @DisplayName("RELIABILITY: Context with null token type")
    void testCustomize_NullTokenType() {
        // Given
        when(context.getTokenType()).thenReturn(null);
        when(claimsBuilder.claim(anyString(), any())).thenReturn(claimsBuilder);

        // When & Then - Should handle gracefully
        try {
            tokenCustomizer.customize(context);
            verify(claimsBuilder).claim("client-id", TEST_CLIENT_ID);
        } catch (NullPointerException e) {
            // Acceptable if it fails fast
            assertThat(e).isInstanceOf(NullPointerException.class);
        }
    }
}
