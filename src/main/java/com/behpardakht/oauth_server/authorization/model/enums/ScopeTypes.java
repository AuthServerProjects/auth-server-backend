package com.behpardakht.oauth_server.authorization.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScopeTypes {

    OPENID("OPENID"),
    PROFILE("PROFILE"),
    EMAIL("EMAIL"),
    ADDRESS("ADDRESS"),
    PHONE("PHONE");

    private final String value;
}