package com.behpardakht.oauth_server.authorization.model.mapper;

import com.behpardakht.oauth_server.authorization.model.dto.client.ClientDto;
import com.behpardakht.oauth_server.authorization.model.dto.client.TokenAndClientSettingDto;
import com.behpardakht.oauth_server.authorization.model.entity.Client;
import com.behpardakht.oauth_server.authorization.model.entity.TokenAndClientSetting;
import com.behpardakht.oauth_server.authorization.model.enums.AuthenticationMethodTypes;
import com.behpardakht.oauth_server.authorization.model.enums.AuthorizationGrantTypes;
import com.behpardakht.oauth_server.authorization.model.enums.ScopeTypes;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ClientMapper {

    public Client registeredClientToEntity(RegisteredClient registeredClient) {
        Client entity = new Client();
        entity.setRegisteredClientId(registeredClient.getId());
        entity.setClientId(registeredClient.getClientId());
        entity.setClientSecret(registeredClient.getClientSecret());
        entity.setClientAuthenticationMethods(
                registeredClient.getClientAuthenticationMethods().stream()
                        .map(ClientAuthenticationMethod::getValue).collect(Collectors.toSet()));
        entity.setAuthorizationGrantTypes(
                registeredClient.getAuthorizationGrantTypes().stream()
                        .map(AuthorizationGrantType::getValue).collect(Collectors.toSet()));
        entity.setRedirectUris(registeredClient.getRedirectUris());
        entity.setScopes(registeredClient.getScopes());
        entity.setSetting(getSetting(registeredClient));
        return entity;
    }

    private TokenAndClientSetting getSetting(RegisteredClient registeredClient) {
        TokenAndClientSetting setting = new TokenAndClientSetting();

        ClientSettings clientSettings = registeredClient.getClientSettings();
        setting.setRequireProofKey(clientSettings.isRequireProofKey());
        setting.setRequireAuthorizationConsent(clientSettings.isRequireAuthorizationConsent());

        TokenSettings tokenSettings = registeredClient.getTokenSettings();
        setting.setAccessTokenTimeToLive(tokenSettings.getAccessTokenTimeToLive().toMinutes());
        setting.setX509CertificateBoundAccessTokens(tokenSettings.isX509CertificateBoundAccessTokens());
        setting.setRefreshTokenTimeToLive(tokenSettings.getRefreshTokenTimeToLive().toMinutes());
        setting.setReuseRefreshTokens(tokenSettings.isReuseRefreshTokens());
        setting.setIdTokenSignatureAlgorithm(tokenSettings.getIdTokenSignatureAlgorithm().getName());
        setting.setAuthorizationCodeTimeToLive(tokenSettings.getAuthorizationCodeTimeToLive().toMinutes());
        setting.setDeviceCodeTimeToLive(tokenSettings.getDeviceCodeTimeToLive().toMinutes());
        return setting;
    }

    public RegisteredClient entityToRegisteredClient(Client entity) {
        return RegisteredClient
                .withId(entity.getRegisteredClientId())
                .clientId(entity.getClientId())
                .clientSecret(entity.getClientSecret())
                .clientAuthenticationMethods(
                        authMethods -> entity.getClientAuthenticationMethods()
                                .forEach(method -> authMethods.add(new ClientAuthenticationMethod(method))))
                .authorizationGrantTypes(
                        grantTypes -> entity.getAuthorizationGrantTypes()
                                .forEach(grant -> grantTypes.add(new AuthorizationGrantType(grant))))
                .redirectUris(uris -> uris.addAll(entity.getRedirectUris()))
                .scopes(scopes -> scopes.addAll(entity.getScopes()))
                .clientSettings(getClientSetting(entity))
                .tokenSettings(getTokenSetting(entity))
                .build();
    }

    private ClientSettings getClientSetting(Client entity) {
        boolean proofKey =
                entity.getSetting().getRequireProofKey() != null ?
                        entity.getSetting().getRequireProofKey() : true;
        boolean authorizationConsent =
                entity.getSetting().getRequireAuthorizationConsent() != null ?
                        entity.getSetting().getRequireAuthorizationConsent() : false;

        return ClientSettings.builder()
                .requireProofKey(proofKey)
                .requireAuthorizationConsent(authorizationConsent)
                .build();
    }

    private TokenSettings getTokenSetting(Client entity) {
        long accessTokenTime =
                entity.getSetting().getAccessTokenTimeToLive() != null ?
                        entity.getSetting().getAccessTokenTimeToLive() : 5L;
        boolean x509Certificate =
                entity.getSetting().getX509CertificateBoundAccessTokens() != null ?
                        entity.getSetting().getX509CertificateBoundAccessTokens() : false;
        long refreshTokenTime =
                entity.getSetting().getRefreshTokenTimeToLive() != null ?
                        entity.getSetting().getRefreshTokenTimeToLive() : 60L;
        boolean reuseRefreshTokens =
                entity.getSetting().getReuseRefreshTokens() != null ?
                        entity.getSetting().getReuseRefreshTokens() : true;
        SignatureAlgorithm idToken =
                entity.getSetting().getIdTokenSignatureAlgorithm() != null ?
                        SignatureAlgorithm.from(entity.getSetting().getIdTokenSignatureAlgorithm()) : SignatureAlgorithm.RS256;
        long authorizationCodeTime =
                entity.getSetting().getAuthorizationCodeTimeToLive() != null ?
                        entity.getSetting().getAuthorizationCodeTimeToLive() : 5L;
        long deviceCodeTime =
                entity.getSetting().getDeviceCodeTimeToLive() != null ?
                        entity.getSetting().getDeviceCodeTimeToLive() : 5L;

        return TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(accessTokenTime))
                .x509CertificateBoundAccessTokens(x509Certificate)
                .refreshTokenTimeToLive(Duration.ofMinutes(refreshTokenTime))
                .reuseRefreshTokens(reuseRefreshTokens)
                .idTokenSignatureAlgorithm(idToken)
                .authorizationCodeTimeToLive(Duration.ofMinutes(authorizationCodeTime))
                .deviceCodeTimeToLive(Duration.ofMinutes(deviceCodeTime))
                .build();
    }

    // -----------------------------------------------------------------------
    public ClientDto registeredClientToDto(RegisteredClient registeredClient) {
        ClientDto dto = new ClientDto();
        dto.setRegisteredClientId(registeredClient.getId());
        dto.setClientId(registeredClient.getClientId());
        dto.setClientSecret(registeredClient.getClientSecret());
        dto.setClientAuthenticationMethods(
                registeredClient.getClientAuthenticationMethods().stream().filter(Objects::nonNull)
                        .map(c -> AuthenticationMethodTypes.valueOf(c.getValue())).collect(Collectors.toSet()));
        dto.setAuthorizationGrantTypes(
                registeredClient.getAuthorizationGrantTypes().stream()
                        .map(c -> AuthorizationGrantTypes.valueOf(c.getValue())).collect(Collectors.toSet()));
        dto.setRedirectUris(registeredClient.getRedirectUris());
        dto.setScopes(getScopeTypes(registeredClient.getScopes()));
        dto.setSetting(getSettingDto(registeredClient));
        return dto;
    }

    private Set<ScopeTypes> getScopeTypes(Set<String> scopes) {
        if (scopes != null && !scopes.isEmpty()) {
            return scopes.stream().filter(Objects::nonNull).map(ScopeTypes::valueOf).collect(Collectors.toSet());
        } else {
            return new HashSet<>(0);
        }
    }

    private TokenAndClientSettingDto getSettingDto(RegisteredClient registeredClient) {
        TokenAndClientSettingDto setting = new TokenAndClientSettingDto();

        ClientSettings clientSettings = registeredClient.getClientSettings();
        setting.setRequireProofKey(clientSettings.isRequireProofKey());
        setting.setRequireAuthorizationConsent(clientSettings.isRequireAuthorizationConsent());

        TokenSettings tokenSettings = registeredClient.getTokenSettings();
        setting.setAccessTokenTimeToLive(tokenSettings.getAccessTokenTimeToLive().toMinutes());
        setting.setX509CertificateBoundAccessTokens(tokenSettings.isX509CertificateBoundAccessTokens());
        setting.setRefreshTokenTimeToLive(tokenSettings.getRefreshTokenTimeToLive().toMinutes());
        setting.setReuseRefreshTokens(tokenSettings.isReuseRefreshTokens());
        setting.setIdTokenSignatureAlgorithm(tokenSettings.getIdTokenSignatureAlgorithm().getName());
        setting.setAuthorizationCodeTimeToLive(tokenSettings.getAuthorizationCodeTimeToLive().toMinutes());
        setting.setDeviceCodeTimeToLive(tokenSettings.getDeviceCodeTimeToLive().toMinutes());
        return setting;
    }

    public RegisteredClient dtoToRegisteredClient(ClientDto dto) {
        return RegisteredClient
                .withId(dto.getRegisteredClientId())
                .clientId(dto.getClientId())
                .clientSecret(dto.getClientSecret())
                .clientAuthenticationMethods(
                        authMethods -> dto.getClientAuthenticationMethods().stream().filter(Objects::nonNull)
                                .map(method -> new ClientAuthenticationMethod(method.getValue())).forEach(authMethods::add))
                .authorizationGrantTypes(
                        grantTypes -> dto.getAuthorizationGrantTypes().stream().filter(Objects::nonNull)
                                .map(grant -> new AuthorizationGrantType(grant.getValue())).forEach(grantTypes::add))
                .redirectUris(uris -> uris.addAll(dto.getRedirectUris()))
                .scopes(scopes -> scopes.addAll(getScope(dto.getScopes())))
                .clientSettings(getClientSetting(dto))
                .tokenSettings(getTokenSetting(dto))
                .build();
    }

    private Set<String> getScope(Set<ScopeTypes> scopeTypes) {
        if (scopeTypes != null && !scopeTypes.isEmpty()) {
            return scopeTypes.stream().filter(Objects::nonNull).map(ScopeTypes::getValue).collect(Collectors.toSet());
        } else {
            return new HashSet<>(0);
        }
    }

    private ClientSettings getClientSetting(ClientDto dto) {
        boolean proofKey =
                dto.getSetting().getRequireProofKey() != null ?
                        dto.getSetting().getRequireProofKey() : true;
        boolean authorizationConsent =
                dto.getSetting().getRequireAuthorizationConsent() != null ?
                        dto.getSetting().getRequireAuthorizationConsent() : false;

        return ClientSettings.builder()
                .requireProofKey(proofKey)
                .requireAuthorizationConsent(authorizationConsent)
                .build();
    }

    private TokenSettings getTokenSetting(ClientDto dto) {
        long accessTokenTime =
                dto.getSetting().getAccessTokenTimeToLive() != null ?
                        dto.getSetting().getAccessTokenTimeToLive() : 5L;
        boolean x509Certificate =
                dto.getSetting().getX509CertificateBoundAccessTokens() != null ?
                        dto.getSetting().getX509CertificateBoundAccessTokens() : false;
        long refreshTokenTime =
                dto.getSetting().getRefreshTokenTimeToLive() != null ?
                        dto.getSetting().getRefreshTokenTimeToLive() : 60L;
        boolean reuseRefreshTokens =
                dto.getSetting().getReuseRefreshTokens() != null ?
                        dto.getSetting().getReuseRefreshTokens() : true;
        SignatureAlgorithm idToken =
                dto.getSetting().getIdTokenSignatureAlgorithm() != null ?
                        SignatureAlgorithm.from(dto.getSetting().getIdTokenSignatureAlgorithm()) : SignatureAlgorithm.RS256;
        long authorizationCodeTime =
                dto.getSetting().getAuthorizationCodeTimeToLive() != null ?
                        dto.getSetting().getAuthorizationCodeTimeToLive() : 5L;
        long deviceCodeTime =
                dto.getSetting().getDeviceCodeTimeToLive() != null ?
                        dto.getSetting().getDeviceCodeTimeToLive() : 5L;

        return TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(accessTokenTime))
                .x509CertificateBoundAccessTokens(x509Certificate)
                .refreshTokenTimeToLive(Duration.ofMinutes(refreshTokenTime))
                .reuseRefreshTokens(reuseRefreshTokens)
                .idTokenSignatureAlgorithm(idToken)
                .authorizationCodeTimeToLive(Duration.ofMinutes(authorizationCodeTime))
                .deviceCodeTimeToLive(Duration.ofMinutes(deviceCodeTime))
                .build();
    }

    // -----------------------------------------------------------------------
    public ClientDto entityToDto(Client entity) {
        if (entity != null) {
            ClientDto dto = new ClientDto();
            dto.setId(entity.getId());
            dto.setRegisteredClientId(entity.getRegisteredClientId());
            dto.setClientId(entity.getClientId());
            dto.setClientSecret(entity.getClientSecret());
            dto.setClientAuthenticationMethods(
                    entity.getClientAuthenticationMethods().stream().filter(Objects::nonNull)
                            .map(AuthenticationMethodTypes::valueOf).collect(Collectors.toSet()));
            dto.setAuthorizationGrantTypes(
                    entity.getAuthorizationGrantTypes().stream().filter(Objects::nonNull)
                            .map(AuthorizationGrantTypes::valueOf).collect(Collectors.toSet()));
            dto.setRedirectUris(entity.getRedirectUris());
            dto.setScopes(getScopeTypes(entity.getScopes()));
            dto.setSetting(getSettingDto(entity.getSetting()));
            dto.setIsEnabled(entity.getIsEnabled());
            return dto;
        }
        return null;
    }

    private TokenAndClientSettingDto getSettingDto(TokenAndClientSetting setting) {
        if (setting != null) {
            TokenAndClientSettingDto dto = new TokenAndClientSettingDto();
            dto.setRequireProofKey(setting.getRequireProofKey());
            dto.setRequireAuthorizationConsent(setting.getRequireAuthorizationConsent());
            dto.setAccessTokenTimeToLive(setting.getAccessTokenTimeToLive());
            dto.setX509CertificateBoundAccessTokens(setting.getX509CertificateBoundAccessTokens());
            dto.setRefreshTokenTimeToLive(setting.getRefreshTokenTimeToLive());
            dto.setReuseRefreshTokens(setting.getReuseRefreshTokens());
            dto.setIdTokenSignatureAlgorithm(setting.getIdTokenSignatureAlgorithm());
            dto.setAuthorizationCodeTimeToLive(setting.getAuthorizationCodeTimeToLive());
            dto.setDeviceCodeTimeToLive(setting.getDeviceCodeTimeToLive());
            return dto;
        }
        return null;
    }

    public List<ClientDto> entityToDtoList(List<Client> entities) {
        if (entities != null && !entities.isEmpty()) {
            return entities.stream().filter(Objects::nonNull).map(this::entityToDto).toList();
        }
        return List.of();
    }

    // -----------------------------------------------------------------------
    public Client dtoToEntity(ClientDto dto) {
        if (dto != null) {
            Client entity = new Client();
            entity.setId(dto.getId());
            entity.setRegisteredClientId(dto.getRegisteredClientId());
            entity.setClientId(dto.getClientId());
            entity.setClientSecret(dto.getClientSecret());
            entity.setClientAuthenticationMethods(
                    dto.getClientAuthenticationMethods().stream().filter(Objects::nonNull)
                            .map(AuthenticationMethodTypes::getValue).collect(Collectors.toSet()));
            entity.setAuthorizationGrantTypes(
                    dto.getAuthorizationGrantTypes().stream().filter(Objects::nonNull)
                            .map(AuthorizationGrantTypes::getValue).collect(Collectors.toSet()));
            entity.setRedirectUris(dto.getRedirectUris());
            entity.setScopes(getScope(dto.getScopes()));
            entity.setSetting(toSettingEntity(dto.getSetting()));
            entity.setIsEnabled(dto.getIsEnabled());
            return entity;
        }
        return null;
    }

    private TokenAndClientSetting toSettingEntity(TokenAndClientSettingDto dto) {
        if (dto != null) {
            TokenAndClientSetting setting = new TokenAndClientSetting();
            setting.setRequireProofKey(dto.getRequireProofKey());
            setting.setRequireAuthorizationConsent(dto.getRequireAuthorizationConsent());
            setting.setAccessTokenTimeToLive(dto.getAccessTokenTimeToLive());
            setting.setX509CertificateBoundAccessTokens(dto.getX509CertificateBoundAccessTokens());
            setting.setRefreshTokenTimeToLive(dto.getRefreshTokenTimeToLive());
            setting.setReuseRefreshTokens(dto.getReuseRefreshTokens());
            setting.setIdTokenSignatureAlgorithm(dto.getIdTokenSignatureAlgorithm());
            setting.setAuthorizationCodeTimeToLive(dto.getAuthorizationCodeTimeToLive());
            setting.setDeviceCodeTimeToLive(dto.getDeviceCodeTimeToLive());
            return setting;
        }
        return null;
    }
}