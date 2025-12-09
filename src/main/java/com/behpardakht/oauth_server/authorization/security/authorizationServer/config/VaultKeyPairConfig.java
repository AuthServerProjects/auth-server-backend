package com.behpardakht.oauth_server.authorization.security.authorizationServer.config;

import com.behpardakht.oauth_server.authorization.config.Properties;
import com.behpardakht.oauth_server.authorization.exception.ExceptionMessage;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class VaultKeyPairConfig extends BaseKeyPairConfig {

    private final Properties properties;
    private final ObjectMapper objectMapper;

    @Bean
    public JWKSource<SecurityContext> jwkSourceFromVault(VaultTemplate vaultTemplate) {
        String vaultPath = properties.getVault().getVaultPath();
        log.info("Loading RSA key pair from Vault at path: {}", vaultPath);
        try {
            VaultKeyData keyData = readKeyDataFromVault(vaultTemplate, vaultPath);
            if (keyData == null || !keyData.isValid()) {
                log.warn("No valid keys found in Vault. Generating new keys.");
                keyData = generateAndStoreKeys(vaultTemplate, vaultPath);
            }
            return buildJwkSource(keyData);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to initialize JWK source from Vault", e);
            throw new CustomException(ExceptionMessage.VAULT_KEY_LOAD_FAILED, e);
        }
    }

    private VaultKeyData readKeyDataFromVault(VaultTemplate vaultTemplate, String vaultPath) {
        VaultResponse response = vaultTemplate.read(vaultPath);
        if (response == null || response.getData() == null || response.getData().get("data") == null) {
            return null;
        }
        return objectMapper.convertValue(response.getData().get("data"), VaultKeyData.class);
    }

    private VaultKeyData generateAndStoreKeys(VaultTemplate vaultTemplate, String vaultPath) {
        try {
            log.info("Generating new RSA key pair ({}-bit)", properties.getVault().getKeySize());

            KeyPairGenerator generator = KeyPairGenerator.getInstance(properties.getVault().getAlgorithm());
            generator.initialize(properties.getVault().getKeySize());
            KeyPair keyPair = generator.generateKeyPair();

            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

            VaultKeyData keyData = VaultKeyData.builder()
                    .privateKeyModulus(encodeToBase64(privateKey.getModulus()))
                    .privateKeyExponent(encodeToBase64(privateKey.getPrivateExponent()))
                    .publicKeyModulus(encodeToBase64(publicKey.getModulus()))
                    .publicKeyExponent(encodeToBase64(publicKey.getPublicExponent()))
                    .keyId(UUID.randomUUID().toString())
                    .build();

            vaultTemplate.write(vaultPath, Map.of("data", keyData));
            log.info("Stored new RSA key pair in Vault with key ID: {}", keyData.getKeyId());

            return keyData;

        } catch (Exception e) {
            log.error("Failed to generate RSA key pair", e);
            throw new CustomException(ExceptionMessage.VAULT_KEY_GENERATION_FAILED, e);
        }
    }

    private String encodeToBase64(BigInteger value) {
        return Base64.getEncoder().encodeToString(value.toByteArray());
    }

    private JWKSource<SecurityContext> buildJwkSource(VaultKeyData keyData) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(properties.getVault().getAlgorithm());
        RSAPrivateKey privateKey = buildPrivateKey(keyFactory, keyData);
        RSAPublicKey publicKey = buildPublicKey(keyFactory, keyData);
        String keyId = keyData.getKeyId() != null ? keyData.getKeyId() : properties.getVault().getKeyId();
        RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(keyId).build();
        log.info("Successfully loaded RSA key pair with key ID: {}", keyId);
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    private RSAPrivateKey buildPrivateKey(KeyFactory keyFactory, VaultKeyData keyData) throws Exception {
        RSAPrivateKeySpec spec = new RSAPrivateKeySpec(
                decodeToBigInteger(keyData.getPrivateKeyModulus()),
                decodeToBigInteger(keyData.getPrivateKeyExponent())
        );
        return (RSAPrivateKey) keyFactory.generatePrivate(spec);
    }

    private RSAPublicKey buildPublicKey(KeyFactory keyFactory, VaultKeyData keyData) throws Exception {
        RSAPublicKeySpec spec = new RSAPublicKeySpec(
                decodeToBigInteger(keyData.getPublicKeyModulus()),
                decodeToBigInteger(keyData.getPublicKeyExponent())
        );
        return (RSAPublicKey) keyFactory.generatePublic(spec);
    }

    private BigInteger decodeToBigInteger(String base64Value) {
        return new BigInteger(Base64.getDecoder().decode(base64Value));
    }
}