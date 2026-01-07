package com.behpardakht.oauth_server.authorization.model.mapper;

import com.behpardakht.oauth_server.authorization.model.dto.client.ClientDropdownDto;
import com.behpardakht.oauth_server.authorization.model.dto.client.ClientDto;
import com.behpardakht.oauth_server.authorization.model.dto.client.TokenAndClientSettingDto;
import com.behpardakht.oauth_server.authorization.model.entity.Client;
import com.behpardakht.oauth_server.authorization.model.entity.TokenAndClientSetting;
import com.behpardakht.oauth_server.authorization.model.enums.AuthenticationMethodTypes;
import com.behpardakht.oauth_server.authorization.model.enums.AuthorizationGrantTypes;
import com.behpardakht.oauth_server.authorization.model.enums.ScopeTypes;
import com.behpardakht.oauth_server.authorization.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ClientMapper {

    private final ClientRepository clientRepository;

    public Client loadEntity(Long id) {
        if (id != null) {
            return clientRepository.findById(id).orElse(null);
        }
        return null;
    }

    public Client registeredClientToEntity(RegisteredClient registeredClient) {
        return Client.builder()
                .registeredClientId(registeredClient.getId())
                .clientId(registeredClient.getClientId())
                .clientSecret(registeredClient.getClientSecret())
                .clientAuthenticationMethods(getClientAuthenticationMethods(registeredClient))
                .authorizationGrantTypes(getAuthorizationGrantTypes(registeredClient))
                .scopes(getScopeTypes(registeredClient.getScopes()))
                .setting(getSetting(registeredClient))
                .build();
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
        return ClientDto.builder()
                .registeredClientId(registeredClient.getId())
                .clientId(registeredClient.getClientId())
                .clientSecret(registeredClient.getClientSecret())
                .clientAuthenticationMethods(getClientAuthenticationMethods(registeredClient))
                .authorizationGrantTypes(getAuthorizationGrantTypes(registeredClient))
                .redirectUris(registeredClient.getRedirectUris())
                .scopes(getScopeTypes(registeredClient.getScopes()))
                .setting(getSettingDto(registeredClient))
                .build();
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
        if (entity == null) return null;
        return ClientDto.builder()
                .id(entity.getId())
                .registeredClientId(entity.getRegisteredClientId())
                .clientId(entity.getClientId())
                .clientSecret(entity.getClientSecret())
                .clientAuthenticationMethods(entity.getClientAuthenticationMethods())
                .authorizationGrantTypes(entity.getAuthorizationGrantTypes())
                .redirectUris(entity.getRedirectUris())
                .scopes(entity.getScopes())
                .setting(toSettingDto(entity.getSetting()))
                .isEnabled(entity.getIsEnabled())
                .build();
    }

    public List<ClientDto> entityToDtoList(List<Client> entities) {
        return Optional.ofNullable(entities).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(this::entityToDto).toList();
    }

    public Client dtoToEntity(ClientDto dto) {
        if (dto == null) return null;
        return Client.builder()
                .registeredClientId(dto.getRegisteredClientId())
                .clientId(dto.getClientId())
                .clientSecret(dto.getClientSecret())
                .clientAuthenticationMethods(dto.getClientAuthenticationMethods())
                .authorizationGrantTypes(dto.getAuthorizationGrantTypes())
                .redirectUris(dto.getRedirectUris())
                .scopes(dto.getScopes())
                .setting(toSettingEntity(dto.getSetting()))
                .isEnabled(dto.getIsEnabled() != null ? dto.getIsEnabled() : true)
                .build();
    }

    public void dtoToEntity(Client entity, ClientDto dto) {
        if (dto == null) return;
        if (dto.getClientAuthenticationMethods() != null && !dto.getClientAuthenticationMethods().isEmpty()) {
            entity.setClientAuthenticationMethods(dto.getClientAuthenticationMethods());
        }
        if (dto.getAuthorizationGrantTypes() != null && !dto.getAuthorizationGrantTypes().isEmpty()) {
            entity.setAuthorizationGrantTypes(dto.getAuthorizationGrantTypes());
        }
        if (dto.getRedirectUris() != null && !dto.getRedirectUris().isEmpty()) {
            entity.setRedirectUris(dto.getRedirectUris());
        }
        if (dto.getScopes() != null && !dto.getScopes().isEmpty()) {
            entity.setScopes(dto.getScopes());
        }
        if (dto.getSetting() != null) {
            updateSettingEntity(entity.getSetting(), dto.getSetting());
        }
        if (dto.getIsEnabled() != null) {
            entity.setIsEnabled(dto.getIsEnabled());
        }
    }

    // -----------------------------------------------------------------------

    private Set<AuthenticationMethodTypes> getClientAuthenticationMethods(RegisteredClient registeredClient) {
        return Optional
                .ofNullable(registeredClient.getClientAuthenticationMethods()).orElse(Collections.emptySet())
                .stream().filter(Objects::nonNull)
                .map(method -> AuthenticationMethodTypes.fromValue(method.getValue()))
                .collect(Collectors.toSet());
    }

    private Set<AuthorizationGrantTypes> getAuthorizationGrantTypes(RegisteredClient registeredClient) {
        return Optional
                .ofNullable(registeredClient.getAuthorizationGrantTypes()).orElse(Collections.emptySet())
                .stream().filter(Objects::nonNull)
                .map(c -> AuthorizationGrantTypes.fromValue(c.getValue()))
                .collect(Collectors.toSet());
    }

    private Set<ScopeTypes> getScopeTypes(Set<String> scopes) {
        return Optional.ofNullable(scopes).orElse(Collections.emptySet())
                .stream().filter(Objects::nonNull).map(ScopeTypes::fromValue).collect(Collectors.toSet());
    }

    private TokenAndClientSetting getSetting(RegisteredClient registeredClient) {
        ClientSettings clientSettings = registeredClient.getClientSettings();
        TokenSettings tokenSettings = registeredClient.getTokenSettings();
        return TokenAndClientSetting.builder()
                .requireProofKey(clientSettings.isRequireProofKey())
                .requireAuthorizationConsent(clientSettings.isRequireAuthorizationConsent())
                .accessTokenTimeToLive(tokenSettings.getAccessTokenTimeToLive().toMinutes())
                .x509CertificateBoundAccessTokens(tokenSettings.isX509CertificateBoundAccessTokens())
                .refreshTokenTimeToLive(tokenSettings.getRefreshTokenTimeToLive().toMinutes())
                .reuseRefreshTokens(tokenSettings.isReuseRefreshTokens())
                .idTokenSignatureAlgorithm(tokenSettings.getIdTokenSignatureAlgorithm().getName())
                .authorizationCodeTimeToLive(tokenSettings.getAuthorizationCodeTimeToLive().toMinutes())
                .deviceCodeTimeToLive(tokenSettings.getDeviceCodeTimeToLive().toMinutes())
                .build();
    }

    private TokenAndClientSettingDto getSettingDto(RegisteredClient registeredClient) {
        ClientSettings clientSettings = registeredClient.getClientSettings();
        TokenSettings tokenSettings = registeredClient.getTokenSettings();
        return TokenAndClientSettingDto.builder()
                .requireProofKey(clientSettings.isRequireProofKey())
                .requireAuthorizationConsent(clientSettings.isRequireAuthorizationConsent())
                .accessTokenTimeToLive(tokenSettings.getAccessTokenTimeToLive().toMinutes())
                .x509CertificateBoundAccessTokens(tokenSettings.isX509CertificateBoundAccessTokens())
                .refreshTokenTimeToLive(tokenSettings.getRefreshTokenTimeToLive().toMinutes())
                .reuseRefreshTokens(tokenSettings.isReuseRefreshTokens())
                .idTokenSignatureAlgorithm(tokenSettings.getIdTokenSignatureAlgorithm().getName())
                .authorizationCodeTimeToLive(tokenSettings.getAuthorizationCodeTimeToLive().toMinutes())
                .deviceCodeTimeToLive(tokenSettings.getDeviceCodeTimeToLive().toMinutes())
                .build();
    }

    // -----------------------------------------------------------------------

    public ClientDropdownDto entityToDropdownDto(Client entity) {
        if (entity == null) return null;
        return ClientDropdownDto.builder()
                .clientId(entity.getId())
                .clientName(entity.getClientId())
                .build();
    }

    public List<ClientDropdownDto> entityToDropdownDtoList(List<Client> entities) {
        return Optional.ofNullable(entities).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(this::entityToDropdownDto).toList();
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
        return ClientSettings.builder()
                .requireProofKey(
                        entity.getSetting().getRequireProofKey() != null ?
                                entity.getSetting().getRequireProofKey() : true)
                .requireAuthorizationConsent(
                        entity.getSetting().getRequireAuthorizationConsent() != null ?
                                entity.getSetting().getRequireAuthorizationConsent() : false)
                .build();
    }

    private TokenSettings getTokenSetting(Client entity) {
        TokenAndClientSetting setting = entity.getSetting();
        return getTokenSettings(
                setting.getAccessTokenTimeToLive(),
                setting.getX509CertificateBoundAccessTokens(),
                setting.getRefreshTokenTimeToLive(),
                setting.getReuseRefreshTokens(),
                setting.getIdTokenSignatureAlgorithm(),
                setting.getAuthorizationCodeTimeToLive(),
                setting.getDeviceCodeTimeToLive());
    }

    private TokenSettings getTokenSettings(Long accessTokenTimeToLive, Boolean x509CertificateBoundAccessTokens,
                                           Long refreshTokenTimeToLive, Boolean reuseRefreshTokens,
                                           String idTokenSignatureAlgorithm, Long authorizationCodeTimeToLive,
                                           Long deviceCodeTimeToLive) {
        return TokenSettings.builder()
                .accessTokenTimeToLive(
                        Duration.ofMinutes(accessTokenTimeToLive != null ? accessTokenTimeToLive : 5L))
                .x509CertificateBoundAccessTokens(
                        x509CertificateBoundAccessTokens != null ? x509CertificateBoundAccessTokens : false)
                .refreshTokenTimeToLive(
                        Duration.ofMinutes(refreshTokenTimeToLive != null ? refreshTokenTimeToLive : 60L))
                .reuseRefreshTokens(
                        reuseRefreshTokens != null ? reuseRefreshTokens : true)
                .idTokenSignatureAlgorithm(
                        idTokenSignatureAlgorithm != null ?
                                SignatureAlgorithm.from(idTokenSignatureAlgorithm) : SignatureAlgorithm.RS256)
                .authorizationCodeTimeToLive(
                        Duration.ofMinutes(authorizationCodeTimeToLive != null ? authorizationCodeTimeToLive : 5L))
                .deviceCodeTimeToLive(
                        Duration.ofMinutes(deviceCodeTimeToLive != null ? deviceCodeTimeToLive : 5L))
                .build();
    }

    private ClientSettings getClientSetting(ClientDto dto) {
        return ClientSettings.builder()
                .requireProofKey(
                        dto.getSetting().getRequireProofKey() != null ? dto.getSetting().getRequireProofKey() : true)
                .requireAuthorizationConsent(
                        dto.getSetting().getRequireAuthorizationConsent() != null ?
                                dto.getSetting().getRequireAuthorizationConsent() : false)
                .build();
    }

    private TokenSettings getTokenSetting(ClientDto dto) {
        TokenAndClientSettingDto setting = dto.getSetting();
        return getTokenSettings(
                setting.getAccessTokenTimeToLive(),
                setting.getX509CertificateBoundAccessTokens(),
                setting.getRefreshTokenTimeToLive(),
                setting.getReuseRefreshTokens(),
                setting.getIdTokenSignatureAlgorithm(),
                setting.getAuthorizationCodeTimeToLive(),
                setting.getDeviceCodeTimeToLive());
    }

    // -----------------------------------------------------------------------

    private TokenAndClientSettingDto toSettingDto(TokenAndClientSetting entity) {
        if (entity == null) return null;
        return TokenAndClientSettingDto.builder()
                .requireProofKey(entity.getRequireProofKey())
                .requireAuthorizationConsent(entity.getRequireAuthorizationConsent())
                .accessTokenTimeToLive(entity.getAccessTokenTimeToLive())
                .x509CertificateBoundAccessTokens(entity.getX509CertificateBoundAccessTokens())
                .refreshTokenTimeToLive(entity.getRefreshTokenTimeToLive())
                .reuseRefreshTokens(entity.getReuseRefreshTokens())
                .idTokenSignatureAlgorithm(entity.getIdTokenSignatureAlgorithm())
                .authorizationCodeTimeToLive(entity.getAuthorizationCodeTimeToLive())
                .deviceCodeTimeToLive(entity.getDeviceCodeTimeToLive())
                .build();
    }

    private TokenAndClientSetting toSettingEntity(TokenAndClientSettingDto dto) {
        if (dto == null) return null;
        return TokenAndClientSetting.builder()
                .requireProofKey(dto.getRequireProofKey())
                .requireAuthorizationConsent(dto.getRequireAuthorizationConsent())
                .accessTokenTimeToLive(dto.getAccessTokenTimeToLive())
                .x509CertificateBoundAccessTokens(dto.getX509CertificateBoundAccessTokens())
                .refreshTokenTimeToLive(dto.getRefreshTokenTimeToLive())
                .reuseRefreshTokens(dto.getReuseRefreshTokens())
                .idTokenSignatureAlgorithm(dto.getIdTokenSignatureAlgorithm())
                .authorizationCodeTimeToLive(dto.getAuthorizationCodeTimeToLive())
                .deviceCodeTimeToLive(dto.getDeviceCodeTimeToLive())
                .build();
    }

    private void updateSettingEntity(TokenAndClientSetting entity, TokenAndClientSettingDto dto) {
        if (dto == null || entity == null) return;
        if (dto.getRequireProofKey() != null) {
            entity.setRequireProofKey(dto.getRequireProofKey());
        }
        if (dto.getRequireAuthorizationConsent() != null) {
            entity.setRequireAuthorizationConsent(dto.getRequireAuthorizationConsent());
        }
        if (dto.getAccessTokenTimeToLive() != null) {
            entity.setAccessTokenTimeToLive(dto.getAccessTokenTimeToLive());
        }
        if (dto.getX509CertificateBoundAccessTokens() != null) {
            entity.setX509CertificateBoundAccessTokens(dto.getX509CertificateBoundAccessTokens());
        }
        if (dto.getRefreshTokenTimeToLive() != null) {
            entity.setRefreshTokenTimeToLive(dto.getRefreshTokenTimeToLive());
        }
        if (dto.getReuseRefreshTokens() != null) {
            entity.setReuseRefreshTokens(dto.getReuseRefreshTokens());
        }
        if (dto.getIdTokenSignatureAlgorithm() != null) {
            entity.setIdTokenSignatureAlgorithm(dto.getIdTokenSignatureAlgorithm());
        }
        if (dto.getAuthorizationCodeTimeToLive() != null) {
            entity.setAuthorizationCodeTimeToLive(dto.getAuthorizationCodeTimeToLive());
        }
        if (dto.getDeviceCodeTimeToLive() != null) {
            entity.setDeviceCodeTimeToLive(dto.getDeviceCodeTimeToLive());
        }
    }
}