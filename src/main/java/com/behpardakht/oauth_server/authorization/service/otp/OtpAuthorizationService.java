package com.behpardakht.oauth_server.authorization.service.otp;

import com.behpardakht.oauth_server.authorization.config.Properties;
import com.behpardakht.oauth_server.authorization.model.dto.user.UsersDto;
import com.behpardakht.oauth_server.authorization.model.enums.PkceMethod;
import com.behpardakht.oauth_server.authorization.service.ClientService;
import com.behpardakht.oauth_server.authorization.service.AdminUserService;
import com.behpardakht.oauth_server.authorization.service.otp.OtpStorageService.SessionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.maskPhoneNumber;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpAuthorizationService {

    @Value("${spring.security.oauth2.authorization-server.issuer-uri}")
    private String issuerUri;
    private final Properties properties;

    private final AdminUserService adminUserService;
    private final ClientService clientService;
    private final OAuth2AuthorizationService authorizationService;

    public String createAuthorization(String authorizationCode, SessionDto sessionDto) {
        String phoneNumber = sessionDto.phoneNumber();
        try {
            UsersDto user = adminUserService.findByPhoneNumber(phoneNumber);
            Authentication principal =
                    new UsernamePasswordAuthenticationToken(phoneNumber, null, user.getAuthorities());

            RegisteredClient registeredClient = clientService.findRegisteredClientByClientId(sessionDto.clientId());
            String redirectUrl = getValidatedRedirectUrl(registeredClient.getRedirectUris(), sessionDto.redirectUri());
            Set<String> authorizedScopes = getAuthorizedScopes(sessionDto, registeredClient);
            OAuth2AuthorizationRequest authorizationRequest = getOAuth2AuthorizationRequest(sessionDto, redirectUrl, authorizedScopes);

            OAuth2Authorization authorization =
                    OAuth2Authorization.withRegisteredClient(registeredClient)
                            .id(UUID.randomUUID().toString())
                            .principalName(phoneNumber)
                            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                            .authorizedScopes(authorizedScopes)
                            .token(getAuthCode(authorizationCode))
                            .attribute(Principal.class.getName(), principal)
                            .attribute(OAuth2AuthorizationRequest.class.getName(), authorizationRequest)
                            .build();
            authorizationService.save(authorization);
            log.info("OAuth2Authorization created for phone: {} with code: {}", maskPhoneNumber(phoneNumber), authorizationCode);
            return redirectUrl;
        } catch (Exception e) {
            log.error("Failed to create OAuth2Authorization for phone: {}", maskPhoneNumber(phoneNumber), e);
            throw new RuntimeException("Failed to create authorization", e);
        }
    }

    private String getValidatedRedirectUrl(Set<String> registeredUris, String requestedUri) {
        if (requestedUri != null) {
            if (registeredUris.contains(requestedUri)) {
                return requestedUri;
            } else {
                log.warn("Invalid redirect_uri attempted: {}", requestedUri);
                throw new IllegalArgumentException("Invalid redirect_uri");
            }
        }
        return registeredUris.iterator().next();
    }

    private Set<String> getAuthorizedScopes(SessionDto sessionDto, RegisteredClient registeredClient) {
        Set<String> requestedScopes;
        if (sessionDto.scope() == null || sessionDto.scope().trim().isEmpty()) {
            requestedScopes = Set.of(OidcScopes.OPENID, OidcScopes.PROFILE);
        } else {
            requestedScopes = Set.of(sessionDto.scope().split("\\s+"));
        }
        return requestedScopes.stream().filter(registeredClient.getScopes()::contains).collect(Collectors.toSet());
    }

    private OAuth2AuthorizationRequest getOAuth2AuthorizationRequest(SessionDto sessionDto, String redirectUrl, Set<String> authorizedScopes) {
        OAuth2AuthorizationRequest.Builder authRequestBuilder =
                OAuth2AuthorizationRequest.authorizationCode()
                        .clientId(sessionDto.clientId())
                        .authorizationUri(issuerUri + "/oauth2/authorize")
                        .redirectUri(redirectUrl)
                        .scopes(authorizedScopes)
                        .state(sessionDto.state() != null ? sessionDto.state() : UUID.randomUUID().toString());
        Map<String, Object> additionalParameters = new HashMap<>();
        if (sessionDto.codeChallenge() != null) {
            validatePkceParameters(sessionDto.codeChallenge(), sessionDto.codeChallengeMethod());
            additionalParameters.put(PkceParameterNames.CODE_CHALLENGE, sessionDto.codeChallenge());
            additionalParameters.put(PkceParameterNames.CODE_CHALLENGE_METHOD, sessionDto.codeChallengeMethod());
        }
        if (!additionalParameters.isEmpty()) {
            authRequestBuilder.additionalParameters(additionalParameters);
        }
        return authRequestBuilder.build();
    }

    private void validatePkceParameters(String codeChallenge, String codeChallengeMethod) {
        if (codeChallengeMethod == null || codeChallengeMethod.isEmpty()) {
            throw new IllegalArgumentException("code_challenge_method is required when code_challenge is provided");
        }
        PkceMethod pkceMethod;
        try {
            pkceMethod = PkceMethod.fromValue(codeChallengeMethod);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        if (pkceMethod.hasFixedChallengeLength()) {
            int expectedLength = pkceMethod.getExpectedChallengeLength();
            if (codeChallenge.length() != expectedLength) {
                log.warn("Invalid code_challenge length for method: {}. Expected: {}, Got: {}",
                        pkceMethod.getValue(), expectedLength, codeChallenge.length());
                throw new IllegalArgumentException("Invalid code_challenge length");
            }
        }
        log.debug("PKCE validation successful for method: {}", pkceMethod.getValue());
    }

    private OAuth2AuthorizationCode getAuthCode(String authorizationCode) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.getExpirationTimeMin().getAuthCode(), ChronoUnit.MINUTES);
        return new OAuth2AuthorizationCode(authorizationCode, issuedAt, expiresAt);
    }
}