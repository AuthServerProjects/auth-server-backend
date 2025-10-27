package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.model.enums.AuthenticationMethodTypes;
import com.behpardakht.oauth_server.authorization.model.enums.AuthorizationGrantTypes;
import com.behpardakht.oauth_server.authorization.model.enums.ScopeTypes;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GeneralService {

    public List<String> loadAuthenticationMethodType() {
        return Arrays.stream(AuthenticationMethodTypes.values())
                .map(AuthenticationMethodTypes::getValue).collect(Collectors.toList());
    }

    public List<String> loadAuthorizationGrantType() {
        return Arrays.stream(AuthorizationGrantTypes.values())
                .map(AuthorizationGrantTypes::getValue).collect(Collectors.toList());
    }

    public List<String> loadScopeType() {
        return Arrays.stream(ScopeTypes.values())
                .map(ScopeTypes::getValue).collect(Collectors.toList());
    }
}