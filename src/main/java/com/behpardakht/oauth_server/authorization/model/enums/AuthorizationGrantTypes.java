package com.behpardakht.oauth_server.authorization.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthorizationGrantTypes {

    AUTHORIZATION_CODE("AUTHORIZATION_CODE"),
    REFRESH_TOKEN("REFRESH_TOKEN"),
    CLIENT_CREDENTIALS("CLIENT_CREDENTIALS");

    private final String value;
}