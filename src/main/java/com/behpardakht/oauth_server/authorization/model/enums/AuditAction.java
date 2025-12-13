package com.behpardakht.oauth_server.authorization.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditAction {

    OTP_SENT("otp_sent"),
    OTP_VERIFIED("otp_verified"),
    OTP_FAILED("otp_failed"),

    LOGIN_SUCCESS("login_success"),
    LOGIN_FAILED("login_failed"),
    LOGOUT("logout"),

    TOKEN_ISSUED("token_issued"),
    TOKEN_REFRESHED("token_refreshed"),
    TOKEN_REVOKED("token_revoked"),

    CLIENT_CREATED("client_created"),
    CLIENT_UPDATED("client_updated"),
    CLIENT_DELETED("client_deleted"),
    SECRET_REGENERATED("secret-regenerated"),

    USER_CREATED("user_created"),
    USER_UPDATED("user_updated"),
    USER_DELETED("user_deleted"),
    RESET_PASSWORD("reset_password"),

    ROLE_CREATED("role_created"),
    ROLE_UPDATED("role_updated"),
    ROLE_DELETED("role_deleted"),
    ROLE_ASSIGNED("role_assigned"),
    ROLE_UNASSIGNED("role_unassigned"),
    ROLE_ASSIGNMENT_DELETED("role_assignment_deleted"),

    STATUS_CHANGED("status_changed"),
    SESSION_REVOKED("session_revoked"),
    ALL_SESSION_REVOKED("all_session_revoked");

    private final String value;
}