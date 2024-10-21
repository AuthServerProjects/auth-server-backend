package com.behpardakht.side_pay.auth.model.dto;

import com.behpardakht.side_pay.auth.enums.AuthenticationMethodTypes;
import com.behpardakht.side_pay.auth.enums.AuthorizationGrantTypes;
import com.behpardakht.side_pay.auth.enums.ScopeTypes;
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