package com.behpardakht.oauth_server.authorization.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthenticationMethodTypes {

    CLIENT_SECRET_BASIC("client_secret_basic"),
    CLIENT_SECRET_POST("client_secret_post"),
    CLIENT_SECRET_JWT("client_secret_jwt"),
    PRIVATE_KEY_JWT("private_key_jwt"),
    NONE("none"),
    TLS_CLIENT_AUTH("tls_client_auth"),
    SELF_SIGNED_TLS_CLIENT_AUTH("self_signed_tls_client_auth");

    private final String value;
}