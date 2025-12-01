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
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ClientMapper {

    public Client registeredClientToEntity(RegisteredClient registeredClient) {
        Client entity = new Client();
        entity.setRegisteredClientId(registeredClient.getId());
        entity.setClientId(registeredClient.getClientId());
        entity.setClientSecret(registeredClient.getClientSecret());
        entity.setClientAuthenticationMethods(getClientAuthenticationMethods(registeredClient));
        entity.setAuthorizationGrantTypes(getAuthorizationGrantTypes(registeredClient));
        entity.setScopes(getScopeTypes(registeredClient.getScopes()));
        entity.setSetting(getSetting(registeredClient));
        return entity;
    }

    public RegisteredClient entityToRegisteredClient(Client entity) {
        return getRegisteredClient(
                entity.getRegisteredClientId(),
                entity.getClientId(),
                entity.getClientSecret(),
                entity.getClientAuthenticationMethods(),
                entity.getAuthorizationGrantTypes(),
                entity.getRedirectUris(),
                entity.getScopes(),
                getClientSetting(entity),
                getTokenSetting(entity));
    }

    // -----------------------------------------------------------------------
    public ClientDto registeredClientToDto(RegisteredClient registeredClient) {
        ClientDto dto = new ClientDto();
        dto.setRegisteredClientId(registeredClient.getId());
        dto.setClientId(registeredClient.getClientId());
        dto.setClientSecret(registeredClient.getClientSecret());
        dto.setClientAuthenticationMethods(getClientAuthenticationMethods(registeredClient));
        dto.setAuthorizationGrantTypes(getAuthorizationGrantTypes(registeredClient));
        dto.setRedirectUris(registeredClient.getRedirectUris());
        dto.setScopes(getScopeTypes(registeredClient.getScopes()));
        dto.setSetting(getSettingDto(registeredClient));
        return dto;
    }

    public RegisteredClient dtoToRegisteredClient(ClientDto dto) {
        return getRegisteredClient(
                dto.getRegisteredClientId(),
                dto.getClientId(),
                dto.getClientSecret(),
                dto.getClientAuthenticationMethods(),
                dto.getAuthorizationGrantTypes(),
                dto.getRedirectUris(),
                dto.getScopes(),
                getClientSetting(dto),
                getTokenSetting(dto));
    }

    // -----------------------------------------------------------------------

    public ClientDto entityToDto(Client entity) {
        if (entity != null) {
            ClientDto dto = new ClientDto();
            dto.setId(entity.getId());
            dto.setRegisteredClientId(entity.getRegisteredClientId());
            dto.setClientId(entity.getClientId());
            dto.setClientSecret(entity.getClientSecret());
            dto.setClientAuthenticationMethods(entity.getClientAuthenticationMethods());
            dto.setAuthorizationGrantTypes(entity.getAuthorizationGrantTypes());
            dto.setRedirectUris(entity.getRedirectUris());
            dto.setScopes(entity.getScopes());
            dto.setSetting(toSettingDto(entity.getSetting()));
            dto.setIsEnabled(entity.getIsEnabled());
            return dto;
        }
        return null;
    }

    public List<ClientDto> entityToDtoList(List<Client> entities) {
        return Optional.ofNullable(entities).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(this::entityToDto).toList();
    }

    public Client dtoToEntity(ClientDto dto) {
        if (dto != null) {
            Client entity = new Client();
            entity.setRegisteredClientId(dto.getRegisteredClientId());
            entity.setClientId(dto.getClientId());
            entity.setClientSecret(dto.getClientSecret());
            entity.setClientAuthenticationMethods(dto.getClientAuthenticationMethods());
            entity.setAuthorizationGrantTypes(dto.getAuthorizationGrantTypes());
            entity.setRedirectUris(dto.getRedirectUris());
            entity.setScopes(dto.getScopes());
            entity.setSetting(toSettingEntity(dto.getSetting()));
            entity.setIsEnabled(dto.getIsEnabled() != null ? dto.getIsEnabled() : true);
            return entity;
        }
        return null;
    }

    public void dtoToEntity(Client entity, ClientDto dto) {
        entity.setClientAuthenticationMethods(dto.getClientAuthenticationMethods());
        entity.setAuthorizationGrantTypes(dto.getAuthorizationGrantTypes());
        entity.setRedirectUris(dto.getRedirectUris());
        entity.setScopes(dto.getScopes());
        entity.setSetting(toSettingEntity(dto.getSetting()));
        entity.setIsEnabled(dto.getIsEnabled());
    }

    // -----------------------------------------------------------------------

    private Set<AuthenticationMethodTypes> getClientAuthenticationMethods(RegisteredClient registeredClient) {
        return Optional
                .ofNullable(registeredClient.getClientAuthenticationMethods()).orElse(Collections.emptySet())
                .stream().filter(Objects::nonNull)
                .map(method -> AuthenticationMethodTypes.valueOf(method.getValue()))
                .collect(Collectors.toSet());
    }

    private Set<AuthorizationGrantTypes> getAuthorizationGrantTypes(RegisteredClient registeredClient) {
        return Optional
                .ofNullable(registeredClient.getAuthorizationGrantTypes()).orElse(Collections.emptySet())
                .stream().filter(Objects::nonNull)
                .map(c -> AuthorizationGrantTypes.valueOf(c.getValue()))
                .collect(Collectors.toSet());
    }

    private Set<ScopeTypes> getScopeTypes(Set<String> scopes) {
        return Optional.ofNullable(scopes).orElse(Collections.emptySet())
                .stream().filter(Objects::nonNull).map(ScopeTypes::valueOf).collect(Collectors.toSet());
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

    // -----------------------------------------------------------------------

    private RegisteredClient getRegisteredClient(String registeredClientId, String clientId, String clientSecret,
                                                 Set<AuthenticationMethodTypes> clientAuthenticationMethods,
                                                 Set<AuthorizationGrantTypes> authorizationGrantTypes,
                                                 Set<String> redirectUris, Set<ScopeTypes> scopes2,
                                                 ClientSettings clientSetting, TokenSettings tokenSetting) {
        return RegisteredClient
                .withId(registeredClientId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethods(authMethodSet ->
                        toClientAuthenticationMethods(clientAuthenticationMethods, authMethodSet))
                .authorizationGrantTypes(grantTypeSet ->
                        toAuthorizationGrantTypes(authorizationGrantTypes, grantTypeSet))
                .redirectUris(uris -> uris.addAll(redirectUris))
                .scopes(scopes -> scopes.addAll(getScope(scopes2)))
                .clientSettings(clientSetting)
                .tokenSettings(tokenSetting)
                .build();
    }

    private void toClientAuthenticationMethods(Set<AuthenticationMethodTypes> entity,
                                               Set<ClientAuthenticationMethod> authMethodSet) {
        Optional
                .ofNullable(entity).orElse(Collections.emptySet())
                .stream().filter(Objects::nonNull)
                .map(method -> new ClientAuthenticationMethod(method.getValue()))
                .forEach(authMethodSet::add);
    }

    private void toAuthorizationGrantTypes(Set<AuthorizationGrantTypes> authorizationGrantTypes,
                                           Set<AuthorizationGrantType> grantTypeSet) {
        Optional
                .ofNullable(authorizationGrantTypes).orElse(Collections.emptySet())
                .stream().filter(Objects::nonNull)
                .map(grant -> new AuthorizationGrantType(grant.getValue()))
                .forEach(grantTypeSet::add);
    }

    private Set<String> getScope(Set<ScopeTypes> scopeTypes) {
        return Optional.ofNullable(scopeTypes).orElse(Collections.emptySet())
                .stream().filter(Objects::nonNull).map(ScopeTypes::getValue).collect(Collectors.toSet());
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

    private TokenAndClientSettingDto toSettingDto(TokenAndClientSetting entity) {
        if (entity != null) {
            TokenAndClientSettingDto dto = new TokenAndClientSettingDto();
            dto.setRequireProofKey(entity.getRequireProofKey());
            dto.setRequireAuthorizationConsent(entity.getRequireAuthorizationConsent());
            dto.setAccessTokenTimeToLive(entity.getAccessTokenTimeToLive());
            dto.setX509CertificateBoundAccessTokens(entity.getX509CertificateBoundAccessTokens());
            dto.setRefreshTokenTimeToLive(entity.getRefreshTokenTimeToLive());
            dto.setReuseRefreshTokens(entity.getReuseRefreshTokens());
            dto.setIdTokenSignatureAlgorithm(entity.getIdTokenSignatureAlgorithm());
            dto.setAuthorizationCodeTimeToLive(entity.getAuthorizationCodeTimeToLive());
            dto.setDeviceCodeTimeToLive(entity.getDeviceCodeTimeToLive());
            return dto;
        }
        return null;
    }

    private TokenAndClientSetting toSettingEntity(TokenAndClientSettingDto dto) {
        if (dto != null) {
            TokenAndClientSetting entity = new TokenAndClientSetting();
            entity.setRequireProofKey(dto.getRequireProofKey());
            entity.setRequireAuthorizationConsent(dto.getRequireAuthorizationConsent());
            entity.setAccessTokenTimeToLive(dto.getAccessTokenTimeToLive());
            entity.setX509CertificateBoundAccessTokens(dto.getX509CertificateBoundAccessTokens());
            entity.setRefreshTokenTimeToLive(dto.getRefreshTokenTimeToLive());
            entity.setReuseRefreshTokens(dto.getReuseRefreshTokens());
            entity.setIdTokenSignatureAlgorithm(dto.getIdTokenSignatureAlgorithm());
            entity.setAuthorizationCodeTimeToLive(dto.getAuthorizationCodeTimeToLive());
            entity.setDeviceCodeTimeToLive(dto.getDeviceCodeTimeToLive());
            return entity;
        }
        return null;
    }
}