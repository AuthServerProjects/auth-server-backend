package com.behpardakht.side_pay.auth.service.otp;

import com.behpardakht.side_pay.auth.model.dto.UsersDto;
import com.behpardakht.side_pay.auth.model.enums.PkceMethod;
import com.behpardakht.side_pay.auth.service.ClientService;
import com.behpardakht.side_pay.auth.service.UserService;
import com.behpardakht.side_pay.auth.service.otp.OtpSessionService.SessionDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.behpardakht.side_pay.auth.util.GeneralUtil.maskPhoneNumber;

@Service
@AllArgsConstructor
@Slf4j
public class OtpAuthorizationService {

    private final UserService userService;
    private final ClientService clientService;
    private final OAuth2AuthorizationService authorizationService;

    public String createAuthorization(String authorizationCode, SessionDto sessionDto) {
        String phoneNumber = sessionDto.phoneNumber();
        try {
            UsersDto user = userService.findByPhoneNumber(phoneNumber);
            Authentication principal =
                    new UsernamePasswordAuthenticationToken(phoneNumber, null, user.getAuthorities());

            RegisteredClient registeredClient = clientService.findRegisteredClientByClientId(sessionDto.clientId());
            String redirectUrl = getValidatedRedirectUrl(registeredClient.getRedirectUris(), sessionDto.redirectUri());

            OAuth2Authorization.Builder authorization =
                    OAuth2Authorization.withRegisteredClient(registeredClient)
                            .id(UUID.randomUUID().toString())
                            .principalName(phoneNumber)
                            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                            .authorizedScopes(getAuthorizedScopes(sessionDto, registeredClient))
                            .token(getAuthCode(authorizationCode))
                            .attribute("Principal", principal);

            if (sessionDto.codeChallenge() != null) {
                validatePkceParameters(sessionDto.codeChallenge(), sessionDto.codeChallengeMethod());
                authorization = authorization
                        .attribute("code_challenge", sessionDto.codeChallenge())
                        .attribute("code_challenge_method", sessionDto.codeChallengeMethod()); // Store both!
            }
            authorizationService.save(authorization.build());

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
                throw new IllegalArgumentException("Invalid redirect_uri: " + requestedUri);
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

    private static OAuth2AuthorizationCode getAuthCode(String authorizationCode) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(5, ChronoUnit.MINUTES);
        return new OAuth2AuthorizationCode(authorizationCode, issuedAt, expiresAt);
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
                throw new IllegalArgumentException(
                        String.format("Invalid code_challenge length for %s method. Expected: %d, Got: %d",
                                pkceMethod.getValue(), expectedLength, codeChallenge.length())
                );
            }
        }
        log.debug("PKCE validation successful for method: {}", pkceMethod.getValue());
    }
}