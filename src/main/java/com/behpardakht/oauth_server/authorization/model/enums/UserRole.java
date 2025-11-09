package com.behpardakht.oauth_server.authorization.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {

    SUPER_ADMIN("SUPER_ADMIN", "Super Administrator - Full system access"),
    ADMIN("ADMIN", "Administrator - Manage users and clients"),
    USER("USER", "Regular user - Standard access");

    private final String value;
    private final String description;

    public String getRoleName() {
        return "ROLE_" + value;
    }

    public static UserRole fromValue(String value) {
        for (UserRole role : UserRole.values()) {
            if (role.getValue().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + value);
    }
}