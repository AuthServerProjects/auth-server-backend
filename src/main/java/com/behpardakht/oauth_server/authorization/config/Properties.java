package com.behpardakht.oauth_server.authorization.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@NoArgsConstructor
@ConfigurationProperties(prefix = "storage", ignoreUnknownFields = false)
public class Properties {

    public final ExpirationTime expirationTime = new ExpirationTime();

    @Data
    @NoArgsConstructor
    public static class ExpirationTime {
        private int initialize;
        private int phoneNumber;
        private int otp;
        private int rateLimit;
        private int lockAccount;
        private int failedAttempts;
        private int authCode;
    }
}