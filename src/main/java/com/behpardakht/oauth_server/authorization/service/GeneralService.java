package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.model.enums.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GeneralService {

    public List<AuthenticationMethodTypes> loadAuthenticationMethodType() {
        return Arrays.stream(AuthenticationMethodTypes.values()).collect(Collectors.toList());
    }

    public List<AuthorizationGrantTypes> loadAuthorizationGrantType() {
        return Arrays.stream(AuthorizationGrantTypes.values()).collect(Collectors.toList());
    }

    public List<ScopeTypes> loadScopeType() {
        return Arrays.stream(ScopeTypes.values()).collect(Collectors.toList());
    }

    public List<PkceMethod> loadPkceMethod() {
        return Arrays.stream(PkceMethod.values()).collect(Collectors.toList());
    }

    public List<UserRole> loadUserRoles() {
        return Arrays.stream(UserRole.values()).collect(Collectors.toList());
    }
}