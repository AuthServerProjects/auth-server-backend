package com.behpardakht.side_pay.auth.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PkceMethod {

    PLAIN("plain", "Plain text code challenge"),
    S256("S256", "SHA256 hashed code challenge");

    private final String value;
    private final String description;

    public static PkceMethod fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        for (PkceMethod method : PkceMethod.values()) {
            if (method.getValue().equals(value)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unsupported PKCE method: " + value +
                ". Supported methods: " + getSupportedMethods());
    }

    public static String getSupportedMethods() {
        return String.join(", ",
                PkceMethod.PLAIN.getValue(),
                PkceMethod.S256.getValue()
        );
    }

    public boolean hasFixedChallengeLength() {
        return this == S256;
    }

    public int getExpectedChallengeLength() {
        return switch (this) {
            case S256 -> 43; // Base64URL encoded SHA256 is always 43 chars
            case PLAIN -> -1; // Variable length for plain
        };
    }
}
