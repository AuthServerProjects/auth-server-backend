package com.behpardakht.side_pay.auth.service;

import com.behpardakht.side_pay.auth.enums.AuthenticationMethodTypes;
import com.behpardakht.side_pay.auth.enums.AuthorizationGrantTypes;
import com.behpardakht.side_pay.auth.enums.ScopeTypes;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeneralService {

    public List<String> loadAuthenticationMethodType() {
        List<String> typeList = new ArrayList<>();
        typeList.add(AuthenticationMethodTypes.CLIENT_SECRET_BASIC.getValue());
        typeList.add(AuthenticationMethodTypes.CLIENT_SECRET_POST.getValue());
        typeList.add(AuthenticationMethodTypes.CLIENT_SECRET_JWT.getValue());
        typeList.add(AuthenticationMethodTypes.PRIVATE_KEY_JWT.getValue());
        typeList.add(AuthenticationMethodTypes.NONE.getValue());
        typeList.add(AuthenticationMethodTypes.TLS_CLIENT_AUTH.getValue());
        typeList.add(AuthenticationMethodTypes.SELF_SIGNED_TLS_CLIENT_AUTH.getValue());
        return typeList;
    }

    public List<String> loadAuthorizationGrantType() {
        List<String> typeList = new ArrayList<>();
        typeList.add(AuthorizationGrantTypes.AUTHORIZATION_CODE.getValue());
        typeList.add(AuthorizationGrantTypes.REFRESH_TOKEN.getValue());
        typeList.add(AuthorizationGrantTypes.CLIENT_CREDENTIALS.getValue());
        return typeList;
    }

    public List<String> loadScopeType() {
        List<String> typeList = new ArrayList<>();
        typeList.add(ScopeTypes.OPENID.getValue());
        typeList.add(ScopeTypes.PROFILE.getValue());
        typeList.add(ScopeTypes.EMAIL.getValue());
        typeList.add(ScopeTypes.ADDRESS.getValue());
        typeList.add(ScopeTypes.PHONE.getValue());
        return typeList;
    }
}