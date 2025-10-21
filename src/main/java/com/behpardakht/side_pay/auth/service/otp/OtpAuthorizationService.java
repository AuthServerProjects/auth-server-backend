package com.behpardakht.side_pay.auth.service.otp;

import com.behpardakht.side_pay.auth.model.dto.UsersDto;
import com.behpardakht.side_pay.auth.service.ClientService;
import com.behpardakht.side_pay.auth.service.UserService;
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

import static com.behpardakht.side_pay.auth.util.GeneralUtil.maskPhoneNumber;

@Service
@AllArgsConstructor
@Slf4j
public class OtpAuthorizationService {

    private final UserService userService;
    private final ClientService clientService;
    private final OtpStorageService otpStorageService;
    private final OAuth2AuthorizationService authorizationService;

    public String createAuthorization(String authorizationCode, String clientId, String phoneNumber) {
        try {
            UsersDto user = userService.findByPhoneNumber(phoneNumber);
            Authentication principal =
                    new UsernamePasswordAuthenticationToken(phoneNumber, null, user.getAuthorities());
            Instant issuedAt = Instant.now();
            Instant expiresAt = issuedAt.plus(5, ChronoUnit.MINUTES);
            OAuth2AuthorizationCode authCode = new OAuth2AuthorizationCode(authorizationCode, issuedAt, expiresAt);
            RegisteredClient registeredClient = clientService.findRegisteredClientByClientId(clientId);
            OAuth2Authorization authorization =
                    OAuth2Authorization.withRegisteredClient(registeredClient)
                            .id(UUID.randomUUID().toString())
                            .principalName(phoneNumber)
                            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                            .authorizedScopes(Set.of(OidcScopes.OPENID, "profile"))
                            .token(authCode)
                            .attribute("Principal", principal)
                            .build();
            authorizationService.save(authorization);
            log.info("OAuth2Authorization created for phone: {} with code: {}",
                    maskPhoneNumber(phoneNumber), authorizationCode);
            return getRedirectUrl(registeredClient.getRedirectUris());
        } catch (Exception e) {
            log.error("Failed to create OAuth2Authorization for phone: {}", maskPhoneNumber(phoneNumber), e);
            throw new RuntimeException("Failed to create authorization", e);
        }
    }

    private static String getRedirectUrl(Set<String> redirectUris) {
        return redirectUris.stream()
                .filter(uri -> uri.contains("callback"))
                .findFirst()
                .orElse(redirectUris.iterator().next());
    }

    public String validateAuthorizationCode(String authorizationCode) {
        try {
            String phoneNumber = otpStorageService.getPhoneNumberByAuthCodeId(authorizationCode);

            if (phoneNumber != null) {
                otpStorageService.removeAuthCode(authorizationCode);
                log.info("Authorization code validated and consumed: {}", authorizationCode);
            } else {
                log.warn("Invalid or expired authorization code: {}", authorizationCode);
            }

            return phoneNumber;
        } catch (Exception e) {
            log.error("Error validating authorization code: {}", authorizationCode, e);
            return null;
        }
    }
}