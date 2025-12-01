package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.model.enums.*;
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

    public List<String> loadPkceMethod() {
        return Arrays.stream(PkceMethod.values())
                .map(PkceMethod::getValue).collect(Collectors.toList());
    }

    public List<String> loadUserRoles() {
        return Arrays.stream(UserRole.values())
                .map(UserRole::getValue).collect(Collectors.toList());
    }
}