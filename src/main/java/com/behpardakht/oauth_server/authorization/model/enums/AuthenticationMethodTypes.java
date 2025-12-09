package com.behpardakht.oauth_server.authorization.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthenticationMethodTypes {

    CLIENT_SECRET_BASIC("client_secret_basic"),
    CLIENT_SECRET_POST("client_secret_post"),
    CLIENT_SECRET_JWT("client_secret_jwt"),
    PRIVATE_KEY_JWT("private_key_jwt"),
    NONE("none"),
    TLS_CLIENT_AUTH("tls_client_auth"),
    SELF_SIGNED_TLS_CLIENT_AUTH("self_signed_tls_client_auth");

    private final String value;

    public static AuthenticationMethodTypes fromValue(String value) {
        for (AuthenticationMethodTypes type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown authentication method: " + value);
    }
}