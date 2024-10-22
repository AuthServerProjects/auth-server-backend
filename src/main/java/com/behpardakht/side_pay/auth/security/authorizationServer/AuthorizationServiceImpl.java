package com.behpardakht.side_pay.auth.security.authorizationServer;

import com.behpardakht.side_pay.auth.model.entity.Authorizations;
import com.behpardakht.side_pay.auth.model.mapper.ClientMapper;
import com.behpardakht.side_pay.auth.repository.AuthorizationRepository;
import com.behpardakht.side_pay.auth.repository.ClientRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AuthorizationServiceImpl implements OAuth2AuthorizationService {

    private final ClientMapper clientMapper;
    private final ClientRepository clientRepository;
    private final AuthorizationRepository authorizationRepository;

    @Override
    @Transactional
    public void save(OAuth2Authorization authorization) {
        Authorizations entity = new Authorizations();
        entity.setAuthorizationId(authorization.getId());
        entity.setRegisteredClientId(authorization.getRegisteredClientId());
        entity.setPrincipalName(authorization.getPrincipalName());
        if (authorization.getAccessToken() != null) {
            entity.setAccessToken(authorization.getAccessToken().getToken().getTokenValue());
        }
        if (authorization.getRefreshToken() != null) {
            entity.setRefreshToken(authorization.getRefreshToken().getToken().getTokenValue());
        }
        authorizationRepository.save(entity);
    }

    @Override
    @Transactional
    public void remove(OAuth2Authorization authorization) {
        authorizationRepository.deleteById(authorization.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public OAuth2Authorization findById(String id) {
        return authorizationRepository.findById(id)
                .map(entity -> {
                    RegisteredClient registeredClient =
                            clientRepository.findById(entity.getRegisteredClientId())
                                    .map(clientMapper::toRegisteredClient)
                                    .orElseThrow(() -> new IllegalArgumentException("Registered client not found"));

                    return OAuth2Authorization.withRegisteredClient(registeredClient)
                            .principalName(entity.getPrincipalName())
                            .token(new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, entity.getAccessToken(), null, null))
                            .refreshToken(entity.getRefreshToken() != null ?
                                    new OAuth2RefreshToken(entity.getRefreshToken(), null) : null)
                            .build();
                })
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        if (tokenType.equals(OAuth2TokenType.ACCESS_TOKEN)) {
            return authorizationRepository.findByAccessToken(token)
                    .map(entity -> {
                        RegisteredClient registeredClient =
                                clientRepository.findById(entity.getRegisteredClientId())
                                        .map(clientMapper::toRegisteredClient)
                                        .orElseThrow(() -> new IllegalArgumentException("Registered client not found"));

                        return OAuth2Authorization.withRegisteredClient(registeredClient)
                                .principalName(entity.getPrincipalName())
                                .token(new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, entity.getAccessToken(), null, null))
                                .build();
                    })
                    .orElse(null);
        } else if (tokenType.equals(OAuth2TokenType.REFRESH_TOKEN)) {
            return authorizationRepository.findByRefreshToken(token)
                    .map(entity -> {
                        RegisteredClient registeredClient =
                                clientRepository.findById(entity.getRegisteredClientId())
                                        .map(clientMapper::toRegisteredClient)
                                        .orElseThrow(() -> new IllegalArgumentException("Registered client not found"));

                        return OAuth2Authorization.withRegisteredClient(registeredClient)
                                .principalName(entity.getPrincipalName())
                                .refreshToken(new OAuth2RefreshToken(entity.getRefreshToken(), null))
                                .build();
                    })
                    .orElse(null);
        }
        return null;
    }
}