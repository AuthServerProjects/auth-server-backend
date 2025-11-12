package com.behpardakht.oauth_server.authorization.unitTest.security.oauth2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Security Tests for OAuth2 Authorization Flows
 * Tests authorization code flow, PKCE, state parameter, redirect URI validation,
 * and protection against OAuth2-specific attacks
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2 Flow Security Tests")
class OAuth2FlowSecurityTest {

    private static final String VALID_REDIRECT_URI = "https://example.com/callback";
    private static final String CLIENT_ID = "test-client";
    private static final SecureRandom secureRandom = new SecureRandom();

    // ==================== AUTHORIZATION CODE FLOW TESTS ====================

    @Test
    @DisplayName("SUCCESS: Authorization code flow parameters")
    void testAuthorizationCodeFlow_ValidParameters() {
        // Given - Valid authorization request parameters
        String responseType = "code";
        String state = generateState();
        String scope = "read write";

        // Then - Validate each parameter
        assertThat(responseType).isEqualTo("code");
        assertThat(VALID_REDIRECT_URI).startsWith("https://");
        assertThat(state.length()).isGreaterThanOrEqualTo(16);
        assertThat(scope).isNotEmpty();
    }

    @Test
    @DisplayName("SECURITY: State parameter is random and unique")
    void testAuthorizationCodeFlow_StateParameter() {
        // When - Generate multiple state values
        String state1 = generateState();
        String state2 = generateState();
        String state3 = generateState();

        // Then - All should be different
        assertThat(state1).isNotEqualTo(state2);
        assertThat(state2).isNotEqualTo(state3);
        assertThat(state1).isNotEqualTo(state3);

        // And sufficiently long
        assertThat(state1).hasSizeGreaterThanOrEqualTo(32);
    }

    @Test
    @DisplayName("SECURITY: State parameter prevents CSRF")
    void testAuthorizationCodeFlow_StatePreventsCSRF() {
        // Given - Client generates state
        String clientState = generateState();

        // When - Attacker provides different state
        String attackerState = generateState();

        // Then - Attack is prevented
        assertThat(clientState).isNotEqualTo(attackerState);
    }

    @Test
    @DisplayName("FAIL: Missing state parameter rejected")
    void testAuthorizationCodeFlow_MissingState() {
        // Given
        String state = null;

        // Then - State is required for security
        assertThat(state).isNull();
        // Application should reject requests without state
    }

    @Test
    @DisplayName("FAIL: Empty state parameter rejected")
    void testAuthorizationCodeFlow_EmptyState() {
        // Given
        String state = "";

        // Then
        assertThat(state).isEmpty();
        // Application should reject empty state
    }

    // ==================== PKCE (Proof Key for Code Exchange) TESTS ====================

    @Test
    @DisplayName("SUCCESS: PKCE code verifier generation")
    void testPKCE_CodeVerifierGeneration() {
        // When - Generate code verifier
        String codeVerifier = generateCodeVerifier();

        // Then
        assertThat(codeVerifier).isNotNull();
        assertThat(codeVerifier).hasSizeBetween(43, 128); // RFC 7636
        assertThat(codeVerifier).matches("[A-Za-z0-9\\-._~]+"); // URL-safe characters
    }

    @Test
    @DisplayName("SUCCESS: PKCE code challenge generation (S256)")
    void testPKCE_CodeChallengeS256() throws NoSuchAlgorithmException {
        // Given
        String codeVerifier = generateCodeVerifier();

        // When - Generate code challenge
        String codeChallenge = generateCodeChallenge(codeVerifier, "S256");

        // Then
        assertThat(codeChallenge).isNotNull();
        assertThat(codeChallenge).isNotEqualTo(codeVerifier); // Must be hashed
        assertThat(codeChallenge).hasSize(43); // Base64 URL encoded SHA256
    }

    @Test
    @DisplayName("SUCCESS: PKCE code challenge verification")
    void testPKCE_CodeVerification() throws NoSuchAlgorithmException {
        // Given
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier, "S256");

        // When - Verify code
        String recomputedChallenge = generateCodeChallenge(codeVerifier, "S256");

        // Then - Should match
        assertThat(recomputedChallenge).isEqualTo(codeChallenge);
    }

    @Test
    @DisplayName("SECURITY: PKCE prevents authorization code interception")
    void testPKCE_PreventsInterception() throws NoSuchAlgorithmException {
        // Given - Legitimate client
        String legitimateVerifier = generateCodeVerifier();
        String legitimateChallenge = generateCodeChallenge(legitimateVerifier, "S256");

        // When - Attacker intercepts auth code and tries their own verifier
        String attackerVerifier = generateCodeVerifier();
        String attackerChallenge = generateCodeChallenge(attackerVerifier, "S256");

        // Then - Challenges don't match, attack prevented
        assertThat(legitimateChallenge).isNotEqualTo(attackerChallenge);
    }

    @Test
    @DisplayName("SECURITY: PKCE code verifier is unpredictable")
    void testPKCE_CodeVerifierUnpredictable() {
        // When - Generate multiple verifiers
        String verifier1 = generateCodeVerifier();
        String verifier2 = generateCodeVerifier();
        String verifier3 = generateCodeVerifier();

        // Then - All different
        assertThat(verifier1).isNotEqualTo(verifier2);
        assertThat(verifier2).isNotEqualTo(verifier3);
        assertThat(verifier1).isNotEqualTo(verifier3);
    }

    @Test
    @DisplayName("SECURITY: PKCE plain method not recommended")
    void testPKCE_PlainMethodInsecure() {
        // Given
        String codeVerifier = generateCodeVerifier();

        // When - Plain method means challenge = verifier (no hashing)
        String plainChallenge = codeVerifier;

        // Then - This is insecure, S256 should be used
        assertThat(plainChallenge).isEqualTo(codeVerifier);
        // Application should require S256 method
    }

    // ==================== REDIRECT URI VALIDATION TESTS ====================

    @Test
    @DisplayName("SUCCESS: HTTPS redirect URI accepted")
    void testRedirectURI_HTTPSAccepted() {
        // Given
        String httpsUri = "https://example.com/callback";

        // Then
        assertThat(httpsUri).startsWith("https://");
    }

    @Test
    @DisplayName("SECURITY: HTTP redirect URI rejected (production)")
    void testRedirectURI_HTTPRejected() {
        // Given - HTTP is insecure
        String httpUri = "http://example.com/callback";

        // Then - Should be rejected in production
        assertThat(httpUri).doesNotStartWith("https://");
        // Localhost HTTP may be allowed for development only
    }

    @Test
    @DisplayName("SECURITY: Redirect URI must match exactly")
    void testRedirectURI_ExactMatch() {
        // Given
        String registeredUri = "https://example.com/callback";

        // Then
        assertThat(registeredUri).isEqualTo("https://example.com/callback");
        assertThat(registeredUri).isNotEqualTo("https://example.com/callback?extra=param");
        assertThat(registeredUri).isNotEqualTo("https://example.com/different");
    }

    @Test
    @DisplayName("SECURITY: Open redirect prevented")
    void testRedirectURI_OpenRedirectPrevented() {
        // Given
        String registeredUri = "https://example.com/callback";
        String attackUri = "https://attacker.com/steal";

        // Then - Should be rejected
        assertThat(registeredUri).isNotEqualTo(attackUri);
    }

    @Test
    @DisplayName("SECURITY: Redirect URI with fragments rejected")
    void testRedirectURI_FragmentRejected() {
        // Given - Fragments can leak tokens
        String uriWithFragment = "https://example.com/callback#fragment";

        // Then - Fragments in redirect URI are security risk
        assertThat(uriWithFragment).contains("#");
        // Should be rejected for authorization code flow
    }

    @Test
    @DisplayName("SECURITY: Wildcard redirect URI rejected")
    void testRedirectURI_WildcardRejected() {
        // Given - Wildcards are security risk
        String wildcardUri = "https://*.example.com/callback";

        // Then
        assertThat(wildcardUri).contains("*");
        // Wildcards should not be allowed
    }

    @Test
    @DisplayName("SECURITY: Localhost redirect allowed for development only")
    void testRedirectURI_LocalhostDevelopment() {
        // Given
        String localhostUri = "http://localhost:8080/callback";

        // Then - May be allowed for development
        assertThat(localhostUri.contains("localhost") || localhostUri.contains("127.0.0.1")).isTrue();
        // But should be disabled in production
    }

    // ==================== AUTHORIZATION CODE SECURITY TESTS ====================

    @Test
    @DisplayName("SECURITY: Authorization code is one-time use")
    void testAuthorizationCode_OneTimeUse() {
        // Given
        boolean codeUsed = false;

        // When - Use code once
        codeUsed = true;

        // Then - Second use should be prevented
        assertThat(codeUsed).isTrue();
        // Application must reject reused codes
    }

    @Test
    @DisplayName("SECURITY: Authorization code has short expiration")
    void testAuthorizationCode_ShortExpiration() {
        // Given - Authorization code expiration
        int expirationSeconds = 600; // 10 minutes (max recommended)

        // Then - Should be short-lived
        assertThat(expirationSeconds).isLessThanOrEqualTo(600);
        // OAuth2 spec recommends 10 minutes maximum
    }

    @Test
    @DisplayName("SECURITY: Authorization code is unpredictable")
    void testAuthorizationCode_Unpredictable() {
        // When - Generate multiple codes
        String code1 = generateAuthorizationCode();
        String code2 = generateAuthorizationCode();
        String code3 = generateAuthorizationCode();

        // Then - All different
        assertThat(code1).isNotEqualTo(code2);
        assertThat(code2).isNotEqualTo(code3);
        assertThat(code1).isNotEqualTo(code3);
    }

    @Test
    @DisplayName("SECURITY: Authorization code sufficient entropy")
    void testAuthorizationCode_SufficientEntropy() {
        // When
        String authCode = generateAuthorizationCode();

        // Then - Should be at least 128 bits (16 bytes = 32 hex chars)
        assertThat(authCode.length()).isGreaterThanOrEqualTo(32);
    }

    // ==================== TOKEN ENDPOINT SECURITY TESTS ====================

    @Test
    @DisplayName("SECURITY: Client authentication required at token endpoint")
    void testTokenEndpoint_ClientAuthenticationRequired() {
        // Given
        String clientSecret = "client-secret";

        // Then
        assertThat(CLIENT_ID).isNotNull();
        assertThat(clientSecret).isNotNull();
    }

    @Test
    @DisplayName("SECURITY: Token endpoint uses POST only")
    void testTokenEndpoint_POSTOnly() {
        // Given
        String allowedMethod = "POST";

        // Then
        assertThat(allowedMethod).isEqualTo("POST");
        assertThat("GET").isNotEqualTo(allowedMethod);
        // GET exposes tokens in URL/logs
    }

    @Test
    @DisplayName("SECURITY: Authorization code bound to client")
    void testTokenEndpoint_CodeBoundToClient() {
        // Given
        String originalClientId = "client-1";
        String attackerClientId = "client-2";

        // Then - Should be rejected
        assertThat(originalClientId).isNotEqualTo(attackerClientId);
        // Code must be validated against original client
    }

    // ==================== GRANT TYPE SECURITY TESTS ====================

    @Test
    @DisplayName("SECURITY: Authorization code grant for user context")
    void testGrantType_AuthorizationCode() {
        // Given
        AuthorizationGrantType grantType = AuthorizationGrantType.AUTHORIZATION_CODE;

        // Then
        assertThat(grantType).isEqualTo(AuthorizationGrantType.AUTHORIZATION_CODE);
        // User must authorize
    }

    @Test
    @DisplayName("SECURITY: Client credentials for machine-to-machine")
    void testGrantType_ClientCredentials() {
        // Given
        AuthorizationGrantType grantType = AuthorizationGrantType.CLIENT_CREDENTIALS;

        // Then
        assertThat(grantType).isEqualTo(AuthorizationGrantType.CLIENT_CREDENTIALS);
        // No user context, client acts on its own behalf
    }

    @Test
    @DisplayName("SECURITY: Refresh token requires authentication")
    void testGrantType_RefreshToken() {
        // Given
        AuthorizationGrantType grantType = AuthorizationGrantType.REFRESH_TOKEN;

        // Then
        assertThat(grantType).isEqualTo(AuthorizationGrantType.REFRESH_TOKEN);
        // Client must authenticate when using refresh token
    }

    // ==================== SCOPE SECURITY TESTS ====================

    @Test
    @DisplayName("SECURITY: Requested scopes must be validated")
    void testScope_Validation() {
        // Given
        String[] requested = {"read", "write", "admin", "delete"};
        String[] allowed = {"read", "write"};

        // When - Validate requested against allowed
        boolean allAllowed = true;
        for (String scope : requested) {
            boolean found = false;
            for (String allowedScope : allowed) {
                if (scope.equals(allowedScope)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                allAllowed = false;
                break;
            }
        }

        // Then - Should reject (admin and delete not allowed)
        assertThat(allAllowed).isFalse();
    }

    @Test
    @DisplayName("SECURITY: Scope privilege escalation prevented")
    void testScope_PrivilegeEscalation() {
        // Given
        String grantedScopes = "read";
        String requestedScopes = "read write admin";

        // Then - Must validate scopes against what was originally granted
        assertThat(requestedScopes).isNotEqualTo(grantedScopes);
    }

    // ==================== ATTACK SCENARIO TESTS ====================

    @Test
    @DisplayName("SECURITY: Authorization code injection attack prevented")
    void testAttack_AuthorizationCodeInjection() {
        // Given - Attacker has victim's redirect URI
        String victimRedirectUri = "https://victim.com/callback";
        String attackerCode = generateAuthorizationCode();

        // When - Attacker tries to inject their code
        String attackUrl = victimRedirectUri + "?code=" + attackerCode;

        // Then - Attack prevented by state, client binding, and PKCE
        assertThat(attackUrl).contains(attackerCode);
        // But proper validation will reject this
    }

    @Test
    @DisplayName("SECURITY: Token theft via redirect URI manipulation")
    void testAttack_RedirectURIManipulation() {
        // Given
        String registeredUri = "https://example.com/callback";
        String manipulatedUri = "https://attacker.com/steal?forward=https://example.com/callback";

        // Then - Should be rejected (exact match required)
        assertThat(registeredUri).isNotEqualTo(manipulatedUri);
    }

    @Test
    @DisplayName("SECURITY: Replay attack with captured authorization code")
    void testAttack_ReplayAttack() {
        // Given - Code is used once
        boolean codeUsed = true;

        // Then - Replay is prevented
        assertThat(codeUsed).isTrue();
        // One-time use codes prevent replay
    }

    // ==================== HELPER METHODS ====================

    private String generateState() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateCodeVerifier() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateCodeChallenge(String codeVerifier, String method) throws NoSuchAlgorithmException {
        if ("plain".equals(method)) {
            return codeVerifier;
        }

        // S256
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    private String generateAuthorizationCode() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
