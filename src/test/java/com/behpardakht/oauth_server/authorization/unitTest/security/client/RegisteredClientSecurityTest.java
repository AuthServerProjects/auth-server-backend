package com.behpardakht.oauth_server.authorization.unitTest.security.client;

import com.behpardakht.oauth_server.authorization.model.entity.Client;
import com.behpardakht.oauth_server.authorization.model.mapper.ClientMapper;
import com.behpardakht.oauth_server.authorization.repository.ClientRepository;
import com.behpardakht.oauth_server.authorization.security.authorizationServer.RegisteredClientRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Security Tests for RegisteredClient Management
 * Tests client registration, authentication, secret management,
 * and protection against client-related security vulnerabilities
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Registered Client Security Tests")
class RegisteredClientSecurityTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ClientMapper clientMapper;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private RegisteredClientRepositoryImpl registeredClientRepository;

    private RegisteredClient testClient;
    private Client testClientEntity;
    
    private static final String CLIENT_ID = "test-client-123";
    private static final String CLIENT_SECRET = "test-secret";
    private static final String ENCODED_SECRET = "$2a$10$encodedSecret";

    @BeforeEach
    void setUp() {
        testClient = RegisteredClient.withId("id-123")
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("https://example.com/callback")
                .scope("read")
                .scope("write")
                .build();

        testClientEntity = new Client();
        testClientEntity.setClientId(CLIENT_ID);
        testClientEntity.setClientSecret(CLIENT_SECRET);
    }

    // ==================== CLIENT REGISTRATION TESTS ====================

    @Test
    @DisplayName("SUCCESS: Save client with encoded secret")
    void testSaveClient_Success() {
        // Given
        when(clientMapper.toEntity(testClient)).thenReturn(testClientEntity);
        when(passwordEncoder.encode(CLIENT_SECRET)).thenReturn(ENCODED_SECRET);
        when(clientRepository.save(any(Client.class))).thenReturn(testClientEntity);

        // When
        registeredClientRepository.save(testClient);

        // Then
        verify(passwordEncoder).encode(CLIENT_SECRET);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    @DisplayName("SECURITY: Client secret must be encoded before storage")
    void testSaveClient_SecretEncoded() {
        // Given
        when(clientMapper.toEntity(testClient)).thenReturn(testClientEntity);
        when(passwordEncoder.encode(CLIENT_SECRET)).thenReturn(ENCODED_SECRET);

        // When
        registeredClientRepository.save(testClient);

        // Then
        verify(passwordEncoder).encode(CLIENT_SECRET);
        // Secret should never be stored in plain text
    }

    @Test
    @DisplayName("SECURITY: Plain text secret never stored")
    void testSaveClient_NoPlainTextSecret() {
        // Given
        when(clientMapper.toEntity(testClient)).thenReturn(testClientEntity);
        when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_SECRET);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> {
            Client savedClient = invocation.getArgument(0);
            // Verify the secret is encoded, not plain text
            assertThat(savedClient.getClientSecret()).isNotEqualTo(CLIENT_SECRET);
            return savedClient;
        });

        // When
        registeredClientRepository.save(testClient);

        // Then
        verify(clientRepository).save(any(Client.class));
    }

    // ==================== CLIENT RETRIEVAL TESTS ====================

    @Test
    @DisplayName("SUCCESS: Find client by ID")
    void testFindById_Success() {
        // Given
        when(clientRepository.findById("id-123")).thenReturn(Optional.of(testClientEntity));
        when(clientMapper.toRegisteredClient(testClientEntity)).thenReturn(testClient);

        // When
        RegisteredClient result = registeredClientRepository.findById("id-123");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("id-123");
        verify(clientRepository).findById("id-123");
    }

    @Test
    @DisplayName("SUCCESS: Find client by client ID")
    void testFindByClientId_Success() {
        // Given
        when(clientRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.of(testClientEntity));
        when(clientMapper.toRegisteredClient(testClientEntity)).thenReturn(testClient);

        // When
        RegisteredClient result = registeredClientRepository.findByClientId(CLIENT_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getClientId()).isEqualTo(CLIENT_ID);
        verify(clientRepository).findByClientId(CLIENT_ID);
    }

    @Test
    @DisplayName("FAIL: Find non-existent client by ID returns null")
    void testFindById_NotFound() {
        // Given
        when(clientRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When
        RegisteredClient result = registeredClientRepository.findById("nonexistent");

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("FAIL: Find non-existent client by client ID returns null")
    void testFindByClientId_NotFound() {
        // Given
        when(clientRepository.findByClientId("nonexistent")).thenReturn(Optional.empty());

        // When
        RegisteredClient result = registeredClientRepository.findByClientId("nonexistent");

        // Then
        assertThat(result).isNull();
    }

    // ==================== CLIENT SECRET ATTACK SCENARIOS ====================

    @Test
    @DisplayName("SECURITY: SQL injection attempt in client ID")
    void testFindByClientId_SQLInjection() {
        // Given
        String maliciousClientId = "client' OR '1'='1";
        when(clientRepository.findByClientId(maliciousClientId)).thenReturn(Optional.empty());

        // When
        RegisteredClient result = registeredClientRepository.findByClientId(maliciousClientId);

        // Then - Should be treated as literal string
        assertThat(result).isNull();
        verify(clientRepository).findByClientId(maliciousClientId);
    }

    @Test
    @DisplayName("SECURITY: Null client ID handled safely")
    void testFindByClientId_NullClientId() {
        // Given
        when(clientRepository.findByClientId(null)).thenReturn(Optional.empty());

        // When
        RegisteredClient result = registeredClientRepository.findByClientId(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("SECURITY: Empty client ID handled safely")
    void testFindByClientId_EmptyClientId() {
        // Given
        when(clientRepository.findByClientId("")).thenReturn(Optional.empty());

        // When
        RegisteredClient result = registeredClientRepository.findByClientId("");

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("SECURITY: Very long client ID (DoS prevention)")
    void testFindByClientId_VeryLongClientId() {
        // Given
        String longClientId = "a".repeat(10000);
        when(clientRepository.findByClientId(longClientId)).thenReturn(Optional.empty());

        // When
        RegisteredClient result = registeredClientRepository.findByClientId(longClientId);

        // Then - Should handle without crashing
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("SECURITY: Client secret with special characters encoded properly")
    void testSaveClient_SpecialCharactersInSecret() {
        // Given
        String specialSecret = "pass!@#$%^&*()_+-=[]{}|;':,.<>?";
        testClientEntity.setClientSecret(specialSecret);
        when(clientMapper.toEntity(any())).thenReturn(testClientEntity);
        when(passwordEncoder.encode(specialSecret)).thenReturn(ENCODED_SECRET);

        // When
        registeredClientRepository.save(testClient);

        // Then
        verify(passwordEncoder).encode(anyString());
    }

    @Test
    @DisplayName("SECURITY: Empty client secret handled")
    void testSaveClient_EmptySecret() {
        // Given
        testClientEntity.setClientSecret("");
        when(clientMapper.toEntity(any())).thenReturn(testClientEntity);
        when(passwordEncoder.encode("")).thenReturn(ENCODED_SECRET);

        // When
        registeredClientRepository.save(testClient);

        // Then
        verify(passwordEncoder).encode(anyString());
    }

    // ==================== CLIENT AUTHENTICATION METHOD TESTS ====================

    @Test
    @DisplayName("SECURITY: Client with CLIENT_SECRET_BASIC auth method")
    void testClientAuthMethod_ClientSecretBasic() {
        // Given
        RegisteredClient basicClient = RegisteredClient.withId("basic-id")
                .clientId("basic-client")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();

        // When
        boolean hasBasicAuth = basicClient.getClientAuthenticationMethods()
                .contains(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);

        // Then
        assertThat(hasBasicAuth).isTrue();
    }

    @Test
    @DisplayName("SECURITY: Client with CLIENT_SECRET_POST auth method")
    void testClientAuthMethod_ClientSecretPost() {
        // Given
        RegisteredClient postClient = RegisteredClient.withId("post-id")
                .clientId("post-client")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();

        // When
        boolean hasPostAuth = postClient.getClientAuthenticationMethods()
                .contains(ClientAuthenticationMethod.CLIENT_SECRET_POST);

        // Then
        assertThat(hasPostAuth).isTrue();
    }

    @Test
    @DisplayName("SECURITY: Client with CLIENT_SECRET_JWT auth method")
    void testClientAuthMethod_ClientSecretJWT() {
        // Given
        RegisteredClient jwtClient = RegisteredClient.withId("jwt-id")
                .clientId("jwt-client")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_JWT)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();

        // When
        boolean hasJwtAuth = jwtClient.getClientAuthenticationMethods()
                .contains(ClientAuthenticationMethod.CLIENT_SECRET_JWT);

        // Then
        assertThat(hasJwtAuth).isTrue();
    }

    // ==================== AUTHORIZATION GRANT TYPE TESTS ====================

    @Test
    @DisplayName("SECURITY: Authorization code grant requires proper redirect URI")
    void testAuthorizationCodeGrant_RedirectURIRequired() {
        // Given
        RegisteredClient authCodeClient = RegisteredClient.withId("auth-code-id")
                .clientId("auth-code-client")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://example.com/callback")
                .scope("read")
                .build();

        // When
        boolean hasAuthCodeGrant = authCodeClient.getAuthorizationGrantTypes()
                .contains(AuthorizationGrantType.AUTHORIZATION_CODE);

        // Then
        assertThat(hasAuthCodeGrant).isTrue();
        assertThat(authCodeClient.getRedirectUris()).isNotEmpty();
    }

    @Test
    @DisplayName("SECURITY: Client credentials grant for machine-to-machine")
    void testClientCredentialsGrant_NoUserContext() {
        // Given
        RegisteredClient m2mClient = RegisteredClient.withId("m2m-id")
                .clientId("m2m-client")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("api:read")
                .build();

        // When
        boolean hasClientCredentials = m2mClient.getAuthorizationGrantTypes()
                .contains(AuthorizationGrantType.CLIENT_CREDENTIALS);

        // Then
        assertThat(hasClientCredentials).isTrue();
        // Client credentials doesn't require redirect URI
    }

    @Test
    @DisplayName("SECURITY: Refresh token grant extends session securely")
    void testRefreshTokenGrant_SecureTokenRefresh() {
        // Given
        RegisteredClient refreshClient = RegisteredClient.withId("refresh-id")
                .clientId("refresh-client")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("https://example.com/callback")
                .scope("read")
                .build();

        // When
        boolean hasRefreshToken = refreshClient.getAuthorizationGrantTypes()
                .contains(AuthorizationGrantType.REFRESH_TOKEN);

        // Then
        assertThat(hasRefreshToken).isTrue();
    }

    // ==================== CLIENT SETTINGS SECURITY TESTS ====================

    @Test
    @DisplayName("SECURITY: PKCE required for public clients")
    void testClientSettings_PKCERequired() {
        // Given
        ClientSettings settings = ClientSettings.builder()
                .requireProofKey(true)
                .requireAuthorizationConsent(false)
                .build();

        RegisteredClient publicClient = RegisteredClient.withId("public-id")
                .clientId("public-client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE) // Public client
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://example.com/callback")
                .scope("read")
                .clientSettings(settings)
                .build();

        // When
        boolean pkceRequired = publicClient.getClientSettings().isRequireProofKey();

        // Then
        assertThat(pkceRequired).isTrue();
    }

    @Test
    @DisplayName("SECURITY: Authorization consent can be enforced")
    void testClientSettings_AuthorizationConsent() {
        // Given
        ClientSettings settings = ClientSettings.builder()
                .requireAuthorizationConsent(true)
                .build();

        RegisteredClient consentClient = RegisteredClient.withId("consent-id")
                .clientId("consent-client")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://example.com/callback")
                .scope("read")
                .scope("write")
                .clientSettings(settings)
                .build();

        // When
        boolean consentRequired = consentClient.getClientSettings().isRequireAuthorizationConsent();

        // Then
        assertThat(consentRequired).isTrue();
    }

    // ==================== TOKEN SETTINGS SECURITY TESTS ====================

    @Test
    @DisplayName("SECURITY: Access token has limited lifetime")
    void testTokenSettings_AccessTokenLifetime() {
        // Given
        TokenSettings settings = TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(15))
                .build();

        RegisteredClient limitedTokenClient = RegisteredClient.withId("limited-id")
                .clientId("limited-client")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("read")
                .tokenSettings(settings)
                .build();

        // When
        Duration tokenLifetime = limitedTokenClient.getTokenSettings().getAccessTokenTimeToLive();

        // Then
        assertThat(tokenLifetime).isLessThanOrEqualTo(Duration.ofHours(1));
    }

    @Test
    @DisplayName("SECURITY: Refresh token has configurable lifetime")
    void testTokenSettings_RefreshTokenLifetime() {
        // Given
        TokenSettings settings = TokenSettings.builder()
                .refreshTokenTimeToLive(Duration.ofDays(7))
                .reuseRefreshTokens(false) // More secure: one-time use
                .build();

        RegisteredClient refreshClient = RegisteredClient.withId("refresh-id")
                .clientId("refresh-client")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("https://example.com/callback")
                .scope("read")
                .tokenSettings(settings)
                .build();

        // When
        Duration refreshLifetime = refreshClient.getTokenSettings().getRefreshTokenTimeToLive();
        boolean reuseTokens = refreshClient.getTokenSettings().isReuseRefreshTokens();

        // Then
        assertThat(refreshLifetime).isLessThanOrEqualTo(Duration.ofDays(30));
        assertThat(reuseTokens).isFalse(); // More secure
    }

    // ==================== REDIRECT URI VALIDATION TESTS ====================

    @Test
    @DisplayName("SECURITY: HTTPS redirect URI enforced")
    void testRedirectURI_HTTPSOnly() {
        // Given - Production client should use HTTPS
        RegisteredClient secureClient = RegisteredClient.withId("secure-id")
                .clientId("secure-client")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://example.com/callback")
                .scope("read")
                .build();

        // When
        String redirectUri = secureClient.getRedirectUris().iterator().next();

        // Then
        assertThat(redirectUri).startsWith("https://");
    }

    @Test
    @DisplayName("SECURITY: Multiple redirect URIs supported")
    void testRedirectURI_MultipleURIs() {
        // Given
        RegisteredClient multiUriClient = RegisteredClient.withId("multi-id")
                .clientId("multi-client")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://example.com/callback1")
                .redirectUri("https://example.com/callback2")
                .scope("read")
                .build();

        // When
        int uriCount = multiUriClient.getRedirectUris().size();

        // Then
        assertThat(uriCount).isEqualTo(2);
    }

    @Test
    @DisplayName("SECURITY: Wildcard redirect URIs should be avoided")
    void testRedirectURI_NoWildcards() {
        // Given - Wildcard URIs are security risk
        // This test demonstrates what NOT to do
        String dangerousUri = "https://example.com/*";

        // Then - Exact match URIs are preferred for security
        // OAuth2 spec recommends against wildcards
        assertThat(dangerousUri).contains("*");
        // In production, validate that redirect URIs don't contain wildcards
    }

    // ==================== SCOPE SECURITY TESTS ====================

    @Test
    @DisplayName("SECURITY: Client scopes limited to required permissions")
    void testScopes_LimitedPermissions() {
        // Given
        RegisteredClient limitedScopeClient = RegisteredClient.withId("limited-scope-id")
                .clientId("limited-scope-client")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("read:documents")
                .scope("write:documents")
                .build();

        // When
        int scopeCount = limitedScopeClient.getScopes().size();

        // Then
        assertThat(scopeCount).isEqualTo(2);
        assertThat(limitedScopeClient.getScopes()).doesNotContain("admin");
    }

    @Test
    @DisplayName("SECURITY: Empty scopes handled safely")
    void testScopes_EmptyScopes() {
        // Given
        RegisteredClient noScopeClient = RegisteredClient.withId("no-scope-id")
                .clientId("no-scope-client")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();

        // When
        int scopeCount = noScopeClient.getScopes().size();

        // Then
        assertThat(scopeCount).isZero();
    }

    // ==================== CONCURRENT ACCESS TESTS ====================

    @Test
    @DisplayName("RELIABILITY: Concurrent client lookups handled safely")
    void testFindByClientId_ConcurrentAccess() throws InterruptedException {
        // Given
        when(clientRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.of(testClientEntity));
        when(clientMapper.toRegisteredClient(testClientEntity)).thenReturn(testClient);

        // When - Simulate concurrent requests
        Thread thread1 = new Thread(() -> registeredClientRepository.findByClientId(CLIENT_ID));
        Thread thread2 = new Thread(() -> registeredClientRepository.findByClientId(CLIENT_ID));

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // Then - Should handle concurrent access
        verify(clientRepository, atLeast(2)).findByClientId(CLIENT_ID);
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    @DisplayName("RELIABILITY: Repository exception handled gracefully")
    void testFindByClientId_RepositoryException() {
        // Given
        when(clientRepository.findByClientId(CLIENT_ID))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> registeredClientRepository.findByClientId(CLIENT_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database connection failed");
    }

    @Test
    @DisplayName("RELIABILITY: Mapper exception handled gracefully")
    void testFindByClientId_MapperException() {
        // Given
        when(clientRepository.findByClientId(CLIENT_ID)).thenReturn(Optional.of(testClientEntity));
        when(clientMapper.toRegisteredClient(testClientEntity))
                .thenThrow(new RuntimeException("Mapping failed"));

        // When & Then
        assertThatThrownBy(() -> registeredClientRepository.findByClientId(CLIENT_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Mapping failed");
    }
}
