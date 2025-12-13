package com.behpardakht.oauth_server.authorization.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserPermission {

    // Dashboard
    DASHBOARD_VIEW("dashboard:view", "View dashboard"),

    // User Management
    USER_READ("user:read", "View users"),
    USER_CREATE("user:create", "Create users"),
    USER_UPDATE("user:update", "Update users"),
    USER_DELETE("user:delete", "Delete/disable users"),
    USER_RESET_PASSWORD("user:reset_password", "Reset user password"),
    USER_MANAGE_ROLES("user:manage_roles", "Add/remove roles from users"),

    // Client Management
    CLIENT_READ("client:read", "View clients"),
    CLIENT_CREATE("client:create", "Create clients"),
    CLIENT_UPDATE("client:update", "Update clients"),
    CLIENT_DELETE("client:delete", "Delete/disable clients"),
    CLIENT_REGENERATE_SECRET("client:regenerate_secret", "Regenerate client secret"),

    // Role Management
    ROLE_READ("role:read", "View roles"),
    ROLE_CREATE("role:create", "Create roles"),
    ROLE_UPDATE("role:update", "Update roles"),
    ROLE_DELETE("role:delete", "Delete roles"),

    // Session Management
    SESSION_READ("session:read", "View sessions"),
    SESSION_REVOKE("session:revoke", "Revoke sessions"),

    // Audit Log
    AUDIT_READ("audit:read", "View audit logs");

    private final String value;
    private final String description;
}