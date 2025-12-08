package com.behpardakht.oauth_server.authorization.security.authorizationServer;

import com.behpardakht.oauth_server.authorization.exception.ExceptionMessage;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
import com.behpardakht.oauth_server.authorization.model.entity.Authorizations;
import com.behpardakht.oauth_server.authorization.repository.AuthorizationRepository;
import com.behpardakht.oauth_server.authorization.service.ClientService;
import com.behpardakht.oauth_server.authorization.util.GeneralUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.Principal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements OAuth2AuthorizationService {

    private final ClientService clientService;
    private final AuthorizationRepository authorizationRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void save(OAuth2Authorization authorization) {
        Authorizations entity = authorizationRepository.findByAuthorizationId(authorization.getId())
                .orElse(new Authorizations());

        entity.setAuthorizationId(authorization.getId());
        entity.setRegisteredClientId(authorization.getRegisteredClientId());

        if (authorization.getPrincipalName() != null) {
            entity.setPrincipalName(authorization.getPrincipalName());
        }

        OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode = authorization.getToken(OAuth2AuthorizationCode.class);
        if (authorizationCode != null) {
            entity.setAuthorizationCode(authorizationCode.getToken().getTokenValue());
            entity.setAuthorizationCodeIssuedAt(authorizationCode.getToken().getIssuedAt());
            entity.setAuthorizationCodeExpiresAt(authorizationCode.getToken().getExpiresAt());
        }

        OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getAccessToken();
        if (accessToken != null) {
            entity.setAccessToken(accessToken.getToken().getTokenValue());
            entity.setAccessTokenIssuedAt(accessToken.getToken().getIssuedAt());
            entity.setAccessTokenExpiresAt(accessToken.getToken().getExpiresAt());
            entity.setAuthorizationCodeConsumed(true);
            entity.setAuthorizationCodeConsumedAt(Instant.now());
        }

        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = authorization.getRefreshToken();
        if (refreshToken != null) {
            entity.setRefreshToken(refreshToken.getToken().getTokenValue());
            entity.setRefreshTokenIssuedAt(refreshToken.getToken().getIssuedAt());
            entity.setRefreshTokenExpiresAt(refreshToken.getToken().getExpiresAt());
        }

        Map<String, Object> attributes = authorization.getAttributes();
        if (attributes != null) {
            entity.setAuthorizationAttributes(serializeAttributes(attributes));
        }
        authorizationRepository.save(entity);
    }

    private String serializeAttributes(Map<String, Object> attributes) {
        try {
            return objectMapper.writeValueAsString(attributes);
        } catch (JsonProcessingException e) {
            throw new CustomException(ExceptionMessage.SERIALIZATION_ERROR, e);
        }
    }

    private Map<String, Object> deserializeAttributes(String data) {
        try {
            return objectMapper.readValue(data, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new CustomException(ExceptionMessage.DESERIALIZATION_ERROR, e);
        }
    }

    @Override
    @Transactional
    public void remove(OAuth2Authorization authorization) {
        String id = authorization.getId();
        if (authorizationRepository.findByAuthorizationId(id).isPresent()) {
            authorizationRepository.deleteByAuthorizationId(id);
            log.debug("Removed authorization: {}", id);
        } else {
            log.warn("Attempted to remove non-existent authorization: {}", id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OAuth2Authorization findById(String id) {
        return authorizationRepository.findByAuthorizationId(id)
                .map(this::buildOAuth2Authorization)
                .orElse(null);
    }

    @Override
    @Transactional
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        if (tokenType != null) {
            if (tokenType.equals(OAuth2TokenType.ACCESS_TOKEN)) {
                return authorizationRepository.findByAccessToken(token)
                        .map(this::buildOAuth2Authorization)
                        .orElse(null);
            } else if (tokenType.equals(OAuth2TokenType.REFRESH_TOKEN)) {
                return authorizationRepository.findByRefreshToken(token)
                        .map(this::buildOAuth2Authorization)
                        .orElse(null);
            } else if (tokenType.getValue().equals(OAuth2ParameterNames.CODE)) {
                return authorizationRepository.findByAuthorizationCode(token)
                        .map(auth -> {
                            if (Boolean.TRUE.equals(auth.getAuthorizationCodeConsumed())) {
                                log.warn("SECURITY ALERT: Authorization code replay attempt detected! " +
                                                "Code: {}, Client: {}, Principal: {}, Originally consumed at: {}",
                                        auth.getAuthorizationCode(),
                                        auth.getRegisteredClientId(),
                                        auth.getPrincipalName(),
                                        auth.getAuthorizationCodeConsumedAt());
                                revokeAllTokensForAuthorization(auth);
                                authorizationRepository.delete(auth);
                                return null;
                            }
                            return buildOAuth2Authorization(auth);
                        })
                        .orElse(null);
            }
        }
        return null;
    }

    private void revokeAllTokensForAuthorization(Authorizations auth) {
        if (auth.getAccessToken() != null) {
            log.warn("Revoking access token due to authorization code replay: {}",
                    GeneralUtil.maskToken(auth.getAccessToken()));
        }

        if (auth.getRefreshToken() != null) {
            log.warn("Revoking refresh token due to authorization code replay: {}",
                    GeneralUtil.maskToken(auth.getRefreshToken()));
        }
    }

    private OAuth2Authorization buildOAuth2Authorization(Authorizations entity) {
        RegisteredClient registeredClient =
                clientService.findRegisteredClientByRegisterClientId(entity.getRegisteredClientId());

        OAuth2Authorization.Builder authorizationBuilder =
                OAuth2Authorization.withRegisteredClient(registeredClient)
                        .id(entity.getAuthorizationId())
                        .principalName(entity.getPrincipalName());

        setAccessToken(entity, authorizationBuilder);
        setRefreshToken(entity, authorizationBuilder);
        setAuthorizationCode(entity, authorizationBuilder);
        setOAuth2AuthorizationAttribute(entity, authorizationBuilder);

        return authorizationBuilder.build();
    }

    private static void setAccessToken(Authorizations entity, OAuth2Authorization.Builder authorizationBuilder) {
        if (entity.getAccessToken() != null) {
            OAuth2AccessToken accessToken = new OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    entity.getAccessToken(),
                    entity.getAccessTokenIssuedAt(),
                    entity.getAccessTokenExpiresAt());
            authorizationBuilder.accessToken(accessToken);
        }
    }

    private static void setRefreshToken(Authorizations entity, OAuth2Authorization.Builder authorizationBuilder) {
        if (entity.getRefreshToken() != null) {
            OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(
                    entity.getRefreshToken(),
                    entity.getRefreshTokenIssuedAt(),
                    entity.getRefreshTokenExpiresAt());
            authorizationBuilder.refreshToken(refreshToken);
        }
    }

    private static void setAuthorizationCode(Authorizations entity, OAuth2Authorization.Builder authorizationBuilder) {
        if (entity.getAuthorizationCode() != null) {
            OAuth2AuthorizationCode authorizationCode = new OAuth2AuthorizationCode(
                    entity.getAuthorizationCode(),
                    entity.getAuthorizationCodeIssuedAt(),
                    entity.getAuthorizationCodeExpiresAt());
            authorizationBuilder.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);
            authorizationBuilder.token(authorizationCode, (metadata) ->
                    metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, Map.of()));
        }
    }

    private void setOAuth2AuthorizationAttribute(Authorizations entity, OAuth2Authorization.Builder authorizationBuilder) {
        if (entity.getAuthorizationAttributes() != null && !entity.getAuthorizationAttributes().trim().isEmpty()) {
            Map<String, Object> attributes = deserializeAttributes(entity.getAuthorizationAttributes());
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof LinkedHashMap) {
                    @SuppressWarnings("unchecked")
                    LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) value;
                    if (OAuth2AuthorizationRequest.class.getName().equals(key)) {
                        OAuth2AuthorizationRequest authRequest = convertMapToAuthorizationRequest(map);
                        authorizationBuilder.attribute(key, authRequest);
                    } else if (Principal.class.getName().equals(key)) {
                        Principal principal = convertMapToPrincipal(map);
                        authorizationBuilder.attribute(key, principal);
                    }
                } else {
                    authorizationBuilder.attribute(key, value);
                }
            }
        }
    }

    private OAuth2AuthorizationRequest convertMapToAuthorizationRequest(LinkedHashMap<String, Object> map) {
        try {
            Object scopesObj = map.get("scopes");
            Set<String> scopes = (scopesObj instanceof Collection<?> collection) ?
                    collection.stream().map(Object::toString).collect(Collectors.toSet()) : Set.of();

            OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest.authorizationCode()
                    .clientId((String) map.get("clientId"))
                    .redirectUri((String) map.get("redirectUri"))
                    .scopes(scopes)
                    .state((String) map.get("state"));

            String authorizationUri = (String) map.get("authorizationUri");
            if (authorizationUri != null) {
                builder.authorizationUri(authorizationUri);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> additionalParameters = (Map<String, Object>) map.getOrDefault("additionalParameters", Map.of());
            if (!additionalParameters.isEmpty()) {
                builder.additionalParameters(additionalParameters);
            }

            return builder.build();

        } catch (Exception e) {
            log.error("Failed to convert map to OAuth2AuthorizationRequest", e);
            throw new CustomException(ExceptionMessage.AUTHORIZATION_REQUEST_CONVERSION_ERROR, e);
        }
    }

    private Principal convertMapToPrincipal(LinkedHashMap<String, Object> map) {
        String principalName = (String) map.get("principal");
        String credentials = (String) map.get("credentials");

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        Object auths = map.get("authorities");
        if (auths instanceof Collection<?>) {
            for (Object obj : (Collection<?>) auths) {
                if (obj instanceof String) {
                    authorities.add(new SimpleGrantedAuthority((String) obj));
                } else if (obj instanceof Map<?, ?> authMap) {
                    Object authority = authMap.get("authority");
                    if (authority != null) {
                        authorities.add(new SimpleGrantedAuthority(authority.toString()));
                    }
                }
            }
        }

        Object details = map.get("details");

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(principalName, credentials, authorities);
        token.setDetails(details);
        return token;
    }
}