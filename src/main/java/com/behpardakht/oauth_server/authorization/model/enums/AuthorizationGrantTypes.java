package com.behpardakht.oauth_server.authorization.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthorizationGrantTypes {

    AUTHORIZATION_CODE("AUTHORIZATION_CODE"),
    REFRESH_TOKEN("REFRESH_TOKEN"),
    CLIENT_CREDENTIALS("CLIENT_CREDENTIALS");

    private final String value;
}