package com.behpardakht.oauth_server.authorization.exception;

import lombok.Getter;

@Getter
public enum Messages {
    // GlobalException
    AUTHENTICATION_FAILED_CREDENTIALS("authentication_failed_credentials"),
    GENERAL_ERROR("general_error"),
    INPUTS_ARE_NOT_VALID("inputs_are_not_valid"),
    NOT_FOUND_WITH("not_found_with"),
    IS_ALREADY_EXIST("is_already_exist"),

    // SecurityHandler
    ACCESS_DENIED("access_denied"),
    TOKEN_EXPIRED("token_expired"),
    TOKEN_INVALID_SIGNATURE("token_invalid_signature"),
    TOKEN_NOT_VALID("token_not_valid"),
    TOKEN_MALFORMED("token_malformed"),
    TOKEN_MISSING("token_missing"),
    TOKEN_INVALID_ISSUER("token_invalid_issuer"),
    TOKEN_INVALID_AUDIENCE("token_invalid_audience"),
    AUTHENTICATION_FAILED("authentication_failed"),

    // AuthService
    INVAlID_AUTH_HEADER("invalid_auth_header"),
    TOKEN_NOT_FOUND("token_not_found"),
    NO_ACTIVE_SESSIONS_FOUND("no_active_sessions_found"),
    LOGOUT_SUCCESS("logout_success"),
    USERNAME_CHANGED_SUCCESS("username_changed_success"),
    PASSWORD_CHANGED_SUCCESS("password_changed_success"),

    // SessionService
    SESSION_REVOKED_SUCCESS("session_revoked_success"),
    SESSIONS_REVOKED_SUCCESS("sessions_revoked_success"),
    SESSIONS_REVOKED_FAILED("sessions_revoked_failed"),

    // OtpService
    OTP_SESSION_INITIALIZED_SUCCESS("otp_session_initialized_success"),
    INVALID_STATE("invalid_state"),
    OTP_SEND_FAILED("otp_send_failed"),
    SYSTEM_BUSY("system_busy"),
    RATE_LIMIT_IP("rate_limit_ip"),
    RATE_LIMIT_PHONE("rate_limit_phone"),
    OTP_ALREADY_SENT("otp_already_sent"),
    OTP_SENT_SUCCESS("otp_sent_success"),
    INVALID_OR_EXPIRED_SESSION("invalid_or_expired_session"),
    PHONE_NUMBER_NOT_FOUND("phone_number_not_found"),
    INVALID_OR_EXPIRED_OTP("invalid_or_expired_otp"),
    CLIENT_ID_NOT_FOUND("client_id_not_found"),
    AUTHORIZATION_CREATION_FAILED("authorization_creation_failed"),

    // User
    USER_REGISTERED_SUCCESS("user_registered_success"),
    USER_UPDATED_SUCCESS("user_updated_success"),
    PASSWORD_SENT_SUCCESS("password_sent_success"),
    ROLE_ASSIGNED_SUCCESS("role_assigned_success"),
    ROLE_REMOVED_SUCCESS("role_removed_success"),

    // Client
    CLIENT_REGISTERED_SUCCESS("client_registered_success"),
    CLIENT_UPDATED_SUCCESS("client_updated_success"),
    CLIENT_SECRET_REGENERATED_SUCCESS("client_secret_regenerated_success"),

    // Role
    ROLE_ADDED_SUCCESS("role_added_success"),
    ROLE_ASSIGNED_TO_USERS("role_assigned_to_users"),
    ROLE_DELETED_SUCCESS("role_deleted_success");

    public final String message;

    Messages(String message) {
        this.message = message;
    }
}