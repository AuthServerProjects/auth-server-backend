package com.behpardakht.oauth_server.authorization.model.dto;

import com.behpardakht.oauth_server.authorization.model.enums.AuthenticationMethodTypes;
import com.behpardakht.oauth_server.authorization.model.enums.AuthorizationGrantTypes;
import com.behpardakht.oauth_server.authorization.model.enums.ScopeTypes;
import lombok.Data;

import java.util.Set;

@Data
public class ClientDto {

    private Long id;
    private String registeredClientId;
    private String clientId;
    private String clientSecret;
    private Set<AuthenticationMethodTypes> clientAuthenticationMethods;
    private Set<AuthorizationGrantTypes> authorizationGrantTypes;
    private Set<String> redirectUris;
    private Set<ScopeTypes> scopes;
    private TokenAndClientSettingDto setting;
}