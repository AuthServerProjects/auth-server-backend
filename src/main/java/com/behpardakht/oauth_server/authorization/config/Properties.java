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

    private final ExpirationTimeMin expirationTimeMin = new ExpirationTimeMin();
    private final Times times = new Times();

    @Data
    @NoArgsConstructor
    public static class ExpirationTimeMin {
        private int initialize;
        private int phoneNumber;
        private int otp;
        private int rateLimit;
        private int lockAccount;
        private int authCode;
        private int blacklistToken;
    }

    @Data
    @NoArgsConstructor
    public static class Times {
        private int failedAttempts;
        private int maxOtpPerIpPerHour;
        private int maxGlobalOtpPerMinute;
        private int maxVerificationAttemptsPerHour;
    }
}