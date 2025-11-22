package com.behpardakht.oauth_server.authorization.service;

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

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "accessToken-blacklist:";
    private static final String REFRESH_TOKEN_BLACKLIST_PREFIX = "refreshToken-blacklist:";

    public void blacklistAccessToken(String token, Instant expiresAt) {
        blacklistToken(token, expiresAt, ACCESS_TOKEN_BLACKLIST_PREFIX);
    }

    public void blacklistRefreshToken(String token, Instant expiresAt) {
        blacklistToken(token, expiresAt, REFRESH_TOKEN_BLACKLIST_PREFIX);
    }

    public void blacklistToken(String token, Instant expiresAt, String constKey) {
        if (token == null || token.isBlank()) {
            log.warn("Attempted to blacklist null or empty access token");
            return;
        }
        long ttlSeconds = calculateTtl(expiresAt);
        if (ttlSeconds <= 0) {
            log.debug("Token already expired, skipping blacklist");
            return;
        }
        String key = constKey + token;
        redisTemplate.opsForValue().set(key, "revoked", Duration.ofSeconds(ttlSeconds));
        log.info("Token blacklisted: {}... (TTL: {} seconds)", maskToken(token), ttlSeconds);
    }

    public boolean isAccessTokenBlacklisted(String token) {
        return isTokenBlacklisted(token, ACCESS_TOKEN_BLACKLIST_PREFIX);
    }

    public boolean isRefreshTokenBlacklisted(String token) {
        return isTokenBlacklisted(token, REFRESH_TOKEN_BLACKLIST_PREFIX);
    }

    public boolean isTokenBlacklisted(String token, String constKey) {
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

    private long calculateTtl(Instant expiresAt) {
        if (expiresAt == null) {
            // Default 30 minutes
            return 1800;
        }
        long ttl = expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
        // Add 60 seconds buffer for clock skew between servers
        return Math.max(ttl + 60, 60);
    }

    public int getBlacklistedTokenCount() {
        try {
            int accessCount = redisTemplate.keys(ACCESS_TOKEN_BLACKLIST_PREFIX + "*").size();
            int refreshCount = redisTemplate.keys(REFRESH_TOKEN_BLACKLIST_PREFIX + "*").size();
            return accessCount + refreshCount;
        } catch (Exception e) {
            log.error("Failed to get blacklisted token count", e);
            return 0;
        }
    }
}