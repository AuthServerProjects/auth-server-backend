package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.model.enums.AuthenticationMethodTypes;
import com.behpardakht.oauth_server.authorization.model.enums.AuthorizationGrantTypes;
import com.behpardakht.oauth_server.authorization.model.enums.PkceMethod;
import com.behpardakht.oauth_server.authorization.model.enums.ScopeTypes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GeneralService {

    public List<AuthenticationMethodTypes> loadAuthenticationMethodType() {
        return List.of(AuthenticationMethodTypes.values());
    }

    public List<AuthorizationGrantTypes> loadAuthorizationGrantType() {
        return List.of(AuthorizationGrantTypes.values());
    }

    public List<ScopeTypes> loadScopeType() {
        return List.of(ScopeTypes.values());
    }

    public List<PkceMethod> loadPkceMethod() {
        return List.of(PkceMethod.values());
    }
}