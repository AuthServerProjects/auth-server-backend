package com.behpardakht.oauth_server.authorization.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionMessage {

    // GlobalException
    AUTHENTICATION_FAILED_CREDENTIALS("authentication_failed_credentials"),
    GENERAL_ERROR("general_error"),
    INPUTS_ARE_NOT_VALID("inputs_are_not_valid"),
    NOT_FOUND_WITH("not_found_with"),
    IS_ALREADY_EXIST("is_already_exist"),
    INVALID_REQUEST("invalid_request"),

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

    // AuthorizationService
    SERIALIZATION_ERROR("serialization_error"),
    DESERIALIZATION_ERROR("deserialization_error"),
    AUTHORIZATION_REQUEST_CONVERSION_ERROR("authorization_request_conversion_error"),

    // VaultKeyPair
    VAULT_KEYS_NOT_FOUND("vault_keys_not_found"),
    VAULT_INVALID_KEY_STRUCTURE("vault_invalid_key_structure"),
    VAULT_KEY_GENERATION_FAILED("vault_key_generation_failed"),
    VAULT_KEY_LOAD_FAILED("vault_key_load_failed"),

    // AuthService
    INVALID_AUTH_HEADER("invalid_auth_header"),
    TOKEN_NOT_FOUND("token_not_found"),
    NO_ACTIVE_SESSIONS_FOUND("no_active_sessions_found"),

    // OtpService
    INVALID_STATE("invalid_state"),
    OTP_SEND_FAILED("otp_send_failed"),
    INVALID_OR_EXPIRED_SESSION("invalid_or_expired_session"),
    PHONE_NUMBER_NOT_FOUND("phone_number_not_found"),
    INVALID_OR_EXPIRED_OTP("invalid_or_expired_otp"),
    CLIENT_ID_NOT_FOUND("client_id_not_found"),
    AUTHORIZATION_CREATION_FAILED("authorization_creation_failed"),
    INVALID_REDIRECT_URI("invalid_redirect_uri"),
    INVALID_PKCE_PARAMETERS("invalid_pkce_parameters"),

    // User
    USERNAME_REQUIRED("username_required"),
    USER_NOT_FOUND("user_not_found"),
    USERNAME_SAME_AS_OLD("username_same_as_old"),
    USERNAME_INCORRECT("username_incorrect"),
    PASSWORD_SAME_AS_OLD("password_same_as_old"),
    PASSWORD_INCORRECT("password_incorrect"),
    USER_CLIENT_NOT_FOUND("user_client_not_found"),
    USER_ALREADY_ASSIGNED("user_already_assigned"),
    USER_BANNED("user_banned"),

    // Role
    ROLE_ASSIGNED_TO_USERS("role_assigned_to_users"),
    ROLE_CLIENT_MISMATCH("role_client_mismatch"),

    // Permission
    PERMISSION_ASSIGNED_TO_ROLE("permission_assigned_to_role"),
    PERMISSION_CLIENT_MISMATCH("permission_client_mismatch");

    public final String message;
}