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

    // User-Client Assignment Management
    USER_ASSIGNMENT_READ("user_assignment:read", "View user assignments"),
    USER_ASSIGNMENT_CREATE("user_assignment:create", "Create user assignments"),
    USER_ASSIGNMENT_UPDATE("user_assignment:update", "Update user in client"),
    USER_ASSIGNMENT_DELETE("user_assignment:delete", "Delete user assignments"),
    USER_ASSIGNMENT_BAN("user_assignment:ban", "Ban users from client"),
    USER_ASSIGNMENT_UNBAN("user_assignment:unban", "Unban users from client"),
    USER_ASSIGNMENT_RESET_PASSWORD("user_assignment:reset_password", "Reset user password"),

    // User Role Assignment Management
    USER_ROLE_ASSIGN("user_role:assign", "Assign roles to users"),
    USER_ROLE_UNASSIGN("user_role:unassign", "Unassign roles from users"),
    USER_ROLE_READ("user_role:read", "View user role assignments"),

    // Role Management
    ROLE_READ("role:read", "View roles"),
    ROLE_CREATE("role:create", "Create roles"),
    ROLE_UPDATE("role:update", "Update roles"),
    ROLE_DELETE("role:delete", "Delete roles"),

    // Permission Management
    PERMISSION_READ("permission:read", "View permissions"),
    PERMISSION_CREATE("permission:create", "Create permissions"),
    PERMISSION_UPDATE("permission:update", "Update permissions"),
    PERMISSION_DELETE("permission:delete", "Delete permissions"),

    // Role-Permission Management
    ROLE_MANAGE_PERMISSIONS("role:manage_permissions", "Add/remove permissions from roles"),

    // Session Management
    SESSION_READ("session:read", "View sessions"),
    SESSION_REVOKE("session:revoke", "Revoke sessions"),

    // Audit Log
    AUDIT_READ("audit:read", "View audit logs");

    private final String value;
    private final String description;
}