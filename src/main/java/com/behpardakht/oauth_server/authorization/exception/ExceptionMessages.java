package com.behpardakht.oauth_server.authorization.exception;

import lombok.Getter;

@Getter
public enum ExceptionMessages {
    INPUTS_ARE_NOT_VALID("inputs_are_not_valid"),
    NOT_FOUND_WITH("not_found_with"),
    IS_ALREADY_EXIST("is_already_exist"),
    SERVICE_UNAVAILABLE("service_unavailable"),

    // AuthService
    INVAlID_AUTH_HEADER("invalid_auth_header"),
    TOKEN_NOT_FOUND("token_not_found"),
    NO_ACTIVE_SESSIONS_FOUND("no_active_sessions_found"),

    // OTP exceptions
    INVALID_STATE("invalid_state"),
    INVALID_OR_EXPIRED_SESSION("invalid_or_expired_session"),
    OTP_SEND_FAILED("otp_send_failed"),
    PHONE_NUMBER_NOT_FOUND("phone_number_not_found"),
    CLIENT_ID_NOT_FOUND("client_id_not_found"),
    AUTHORIZATION_CREATION_FAILED("authorization_creation_failed"),
    INVALID_OR_EXPIRED_OTP("invalid_or_expired_otp"),

    // Security Handler exceptions
    ACCESS_DENIED("access_denied"),
    TOKEN_EXPIRED("token_expired"),
    TOKEN_INVALID_SIGNATURE("token_invalid_signature"),
    TOKEN_NOT_YET_VALID("token_not_yet_valid"),
    TOKEN_MALFORMED("token_malformed"),
    TOKEN_MISSING("token_missing"),
    TOKEN_INVALID_ISSUER("token_invalid_issuer"),
    TOKEN_INVALID_AUDIENCE("token_invalid_audience"),
    AUTHENTICATION_FAILED("authentication_failed");

    public final String message;

    ExceptionMessages(String message) {
        this.message = message;
    }
}