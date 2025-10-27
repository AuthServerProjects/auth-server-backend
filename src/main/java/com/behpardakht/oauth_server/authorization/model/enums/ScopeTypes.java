package com.behpardakht.oauth_server.authorization.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScopeTypes {

    OPENID("openid"),
    PROFILE("profile"),
    EMAIL("email"),
    ADDRESS("address"),
    PHONE("phone");

    private final String value;
}