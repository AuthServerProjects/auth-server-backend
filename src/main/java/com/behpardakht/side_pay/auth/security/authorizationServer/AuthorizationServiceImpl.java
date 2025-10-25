package com.behpardakht.side_pay.auth.security.authorizationServer;

import com.behpardakht.side_pay.auth.model.entity.Authorizations;
import com.behpardakht.side_pay.auth.repository.AuthorizationRepository;
import com.behpardakht.side_pay.auth.service.ClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;

@Service
@AllArgsConstructor
public class AuthorizationServiceImpl implements OAuth2AuthorizationService {

    private final ClientService clientService;
    private final AuthorizationRepository authorizationRepository;

    @Override
    @Transactional
    public void save(OAuth2Authorization authorization) {
        Authorizations entity = authorizationRepository.findByAuthorizationId(authorization.getId())
                .orElse(new Authorizations());

        entity.setAuthorizationId(authorization.getId());

        if (authorization.getPrincipalName() != null) {
            entity.setRegisteredClientId(authorization.getRegisteredClientId());
            entity.setPrincipalName(authorization.getPrincipalName());
        }

        OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode = authorization.getToken(OAuth2AuthorizationCode.class);
        if (authorizationCode != null) {
            entity.setAuthorizationCode(authorizationCode.getToken().getTokenValue());
            entity.setAuthorizationCodeIssuedAt(authorizationCode.getToken().getIssuedAt());
            entity.setAuthorizationCodeExpiresAt(authorizationCode.getToken().getExpiresAt());
        }

        OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getToken(OAuth2AccessToken.class);
        if (accessToken != null) {
            entity.setAccessToken(accessToken.getToken().getTokenValue());
            entity.setAccessTokenIssuedAt(accessToken.getToken().getIssuedAt());
            entity.setAccessTokenExpiresAt(accessToken.getToken().getExpiresAt());
        }

        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = authorization.getToken(OAuth2RefreshToken.class);
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
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(attributes);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize attributes", e);
        }
    }

    private Map<String, Object> deserializeAttributes(String data) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(data, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Unable to deserialize attributes", e);
        }
    }

    @Override
    @Transactional
    public void remove(OAuth2Authorization authorization) {
        authorizationRepository.deleteById(authorization.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public OAuth2Authorization findById(String id) {
        Authorizations entity = authorizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Authorization not found"));
        return buildOAuth2Authorization(entity);
    }

    @Override
    @Transactional(readOnly = true)
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
                        .map(this::buildOAuth2Authorization)
                        .orElse(null);
            }
        }
        return null;
    }

    private OAuth2Authorization buildOAuth2Authorization(Authorizations entity) {
        RegisteredClient registeredClient = clientService.findRegisteredClientByRegisterClientId(entity.getRegisteredClientId());
        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(entity.getPrincipalName());

        if (entity.getAccessToken() != null) {
            OAuth2AccessToken accessToken = new OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    entity.getAccessToken(),
                    entity.getAccessTokenIssuedAt(),
                    entity.getAccessTokenExpiresAt());
            authorizationBuilder.token(accessToken);
        }

        if (entity.getRefreshToken() != null) {
            OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(
                    entity.getRefreshToken(),
                    entity.getRefreshTokenIssuedAt(),
                    entity.getRefreshTokenExpiresAt());
            authorizationBuilder.refreshToken(refreshToken);
        }

        if (entity.getAuthorizationCode() != null) {
            OAuth2AuthorizationCode authorizationCode = new OAuth2AuthorizationCode(
                    entity.getAuthorizationCode(),
                    entity.getAuthorizationCodeIssuedAt(),
                    entity.getAuthorizationCodeExpiresAt());
            authorizationBuilder.token(authorizationCode);
            authorizationBuilder.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);
        }
        return authorizationBuilder.build();
    }
}