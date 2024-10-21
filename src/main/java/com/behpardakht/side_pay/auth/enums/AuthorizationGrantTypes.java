package com.behpardakht.side_pay.auth.enums;

public enum AuthorizationGrantTypes {

    AUTHORIZATION_CODE("authorization_code"),
    REFRESH_TOKEN("refresh_token"),
    CLIENT_CREDENTIALS("client_credentials"),
    OTP("otp");

    private final String value;

    AuthorizationGrantTypes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}