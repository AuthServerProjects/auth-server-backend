package com.behpardakht.oauth_server.authorization.exception;

import lombok.Getter;

@Getter
public enum ExceptionMessages {

    INPUTS_ARE_NOT_VALID("inputs_are_not_valid"),
    NOT_FOUND_WITH("not_found_with"),
    IS_ALREADY_EXIST("is_already_exist"),
    SERVICE_UNAVAILABLE("service_unavailable");

    public final String message;

    ExceptionMessages(String message) {
        this.message = message;
    }
}