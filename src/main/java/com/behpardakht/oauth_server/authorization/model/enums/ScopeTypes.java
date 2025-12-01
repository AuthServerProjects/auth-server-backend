package com.behpardakht.oauth_server.authorization.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScopeTypes {

    OPENID("OPENID"),
    PROFILE("PROFILE"),
    EMAIL("EMAIL"),
    ADDRESS("ADDRESS"),
    PHONE("PHONE");

    private final String value;
}