package com.behpardakht.oauth_server.authorization.security.authorizationServer.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaultKeyData {
    private String privateKeyModulus;
    private String privateKeyExponent;
    private String publicKeyModulus;
    private String publicKeyExponent;
    private String keyId;

    public boolean isValid() {
        return privateKeyModulus != null
                && privateKeyExponent != null
                && publicKeyModulus != null
                && publicKeyExponent != null;
    }
}