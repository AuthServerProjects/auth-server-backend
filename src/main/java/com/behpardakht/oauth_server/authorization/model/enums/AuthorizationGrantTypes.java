package com.behpardakht.oauth_server.authorization.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthorizationGrantTypes {

    AUTHORIZATION_CODE("authorization_code"),
    REFRESH_TOKEN("refresh_token"),
    CLIENT_CREDENTIALS("client_credentials");

    private final String value;

    public static AuthorizationGrantTypes fromValue(String value) {
        for (AuthorizationGrantTypes type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown grant type: " + value);
    }
}