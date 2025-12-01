package com.behpardakht.oauth_server.authorization.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthenticationMethodTypes {

    CLIENT_SECRET_BASIC("CLIENT_SECRET_BASIC"),
    CLIENT_SECRET_POST("CLIENT_SECRET_POST"),
    CLIENT_SECRET_JWT("CLIENT_SECRET_JWT"),
    PRIVATE_KEY_JWT("PRIVATE_KEY_JWT"),
    NONE("NONE"),
    TLS_CLIENT_AUTH("TLS_CLIENT_AUTH"),
    SELF_SIGNED_TLS_CLIENT_AUTH("SELF_SIGNED_TLS_CLIENT_AUTH");

    private final String value;
}