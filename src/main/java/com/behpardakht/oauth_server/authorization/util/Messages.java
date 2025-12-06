package com.behpardakht.oauth_server.authorization.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Messages {

    // AuthService
    LOGOUT_SUCCESS("logout_success"),
    USERNAME_CHANGED_SUCCESS("username_changed_success"),
    PASSWORD_CHANGED_SUCCESS("password_changed_success"),

    // SessionService
    SESSION_REVOKED_SUCCESS("session_revoked_success"),
    SESSIONS_REVOKED_SUCCESS("sessions_revoked_success"),
    SESSIONS_REVOKED_FAILED("sessions_revoked_failed"),

    // OtpService
    OTP_SESSION_INITIALIZED_SUCCESS("otp_session_initialized_success"),
    OTP_SEND_FAILED("otp_send_failed"),
    SYSTEM_BUSY("system_busy"),
    RATE_LIMIT_IP("rate_limit_ip"),
    RATE_LIMIT_PHONE("rate_limit_phone"),
    OTP_ALREADY_SENT("otp_already_sent"),
    OTP_SENT_SUCCESS("otp_sent_success"),

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
    ROLE_DELETED_SUCCESS("role_deleted_success");

    public final String message;
}