package com.behpardakht.oauth_server.authorization.security.authorizationServer.config;

import com.behpardakht.oauth_server.authorization.config.Properties;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "vault.enabled", havingValue = "true")
public class VaultKeyPairConfig extends BaseKeyPairConfig{

    private final Properties properties;

    @Bean
    public JWKSource<SecurityContext> jwkSourceFromVault(VaultTemplate vaultTemplate) throws Exception {
        String vaultPath = properties.getVault().getVaultPath();
        log.info("Loading RSA key pair from Vault at path: {}", vaultPath);

        VaultResponse response = vaultTemplate.read(vaultPath);
        if (response == null || response.getData() == null) {
            log.warn("No keys found in Vault. Generating and storing new keys.");
            return generateAndStoreKeysInVault(vaultTemplate);
        }
        Map<String, Object> data = response.getData();

        Map<String, Object> secretData = (Map<String, Object>) data.get("data");
        if (secretData == null || !secretData.containsKey("privateKeyModulus")) {
            log.warn("Invalid key structure in Vault. Regenerating keys.");
            return generateAndStoreKeysInVault(vaultTemplate);
        }
        String privateModulus = (String) secretData.get("privateKeyModulus");
        String privateExponent = (String) secretData.get("privateKeyExponent");
        String publicModulus = (String) secretData.get("publicKeyModulus");
        String publicExponent = (String) secretData.get("publicKeyExponent");
        String storedKeyId = (String) secretData.get("keyId");

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        RSAPrivateKeySpec privateKeySpec = new RSAPrivateKeySpec(
                new BigInteger(Base64.getDecoder().decode(privateModulus)),
                new BigInteger(Base64.getDecoder().decode(privateExponent))
        );
        RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);

        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(
                new BigInteger(Base64.getDecoder().decode(publicModulus)),
                new BigInteger(Base64.getDecoder().decode(publicExponent))
        );
        RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(storedKeyId != null ? storedKeyId : properties.getVault().getKeyId())
                .build();

        log.info("Successfully loaded RSA key pair from Vault with key ID: {}", rsaKey.getKeyID());
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    private JWKSource<SecurityContext> generateAndStoreKeysInVault(VaultTemplate vaultTemplate) throws Exception {
        log.info("Generating new RSA key pair (2048-bit)");
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();

        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        String generatedKeyId = UUID.randomUUID().toString();

        Map<String, Object> keyData = Map.of(
                "data", Map.of(
                        "privateKeyModulus", Base64.getEncoder().encodeToString(privateKey.getModulus().toByteArray()),
                        "privateKeyExponent", Base64.getEncoder().encodeToString(privateKey.getPrivateExponent().toByteArray()),
                        "publicKeyModulus", Base64.getEncoder().encodeToString(publicKey.getModulus().toByteArray()),
                        "publicKeyExponent", Base64.getEncoder().encodeToString(publicKey.getPublicExponent().toByteArray()),
                        "keyId", generatedKeyId,
                        "algorithm", "RS256",
                        "keySize", "2048"));
        vaultTemplate.write(properties.getVault().getVaultPath(), keyData);
        log.info("Successfully stored new RSA key pair in Vault with key ID: {}", generatedKeyId);

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(generatedKeyId)
                .build();
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }
}