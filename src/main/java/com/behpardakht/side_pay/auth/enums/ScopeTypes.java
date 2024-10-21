package com.behpardakht.side_pay.auth.enums;

public enum ScopeTypes {

    OPENID("openid"),
    PROFILE("profile"),
    EMAIL("email"),
    ADDRESS("address"),
    PHONE("phone");

    private final String value;

    ScopeTypes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}