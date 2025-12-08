package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.config.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.maskToken;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final Properties properties;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ACCESS_TOKEN_BLACKLIST_KEY = "blacklist:access:";
    private static final String REFRESH_TOKEN_BLACKLIST_KEY = "blacklist:refresh:";

    public void blacklistAccessToken(String token, Instant expiresAt) {
        blacklistToken(token, expiresAt, ACCESS_TOKEN_BLACKLIST_KEY);
    }

    public void blacklistRefreshToken(String token, Instant expiresAt) {
        blacklistToken(token, expiresAt, REFRESH_TOKEN_BLACKLIST_KEY);
    }

    private void blacklistToken(String token, Instant expiresAt, String prefix) {
        if (token == null || token.isBlank()) {
            log.warn("Attempted to blacklist null or empty token");
            return;
        }
        String key = prefix + token;
        Duration ttl = calculateTtl(expiresAt);
        redisTemplate.opsForValue().set(key, "revoked", ttl);
        log.info("Token blacklisted: {} (TTL: {} seconds)", maskToken(token), ttl.getSeconds());
    }

    private Duration calculateTtl(Instant expiresAt) {
        if (expiresAt == null) {
            return Duration.ofMinutes(properties.getStorage().getExpirationTimeMin().getBlacklistToken());
        }
        long ttlSeconds = Duration.between(Instant.now(), expiresAt).getSeconds();
        return Duration.ofSeconds(Math.max(ttlSeconds + 60, 60));
    }

    public boolean isAccessTokenBlacklisted(String token) {
        return isTokenBlacklisted(token, ACCESS_TOKEN_BLACKLIST_KEY);
    }

    public boolean isRefreshTokenBlacklisted(String token) {
        return isTokenBlacklisted(token, REFRESH_TOKEN_BLACKLIST_KEY);
    }

    private boolean isTokenBlacklisted(String token, String constKey) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String key = constKey + token;
        Boolean exists = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            log.warn("SECURITY: Blocked attempt to use blacklisted token: {}...", maskToken(token));
            return true;
        }
        return false;
    }
}