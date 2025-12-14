package com.behpardakht.oauth_server.authorization.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class Properties {

    private final SuperAdmin superAdmin = new SuperAdmin();
    private final Vault vault = new Vault();
    private final Storage storage = new Storage();
    private final Cleanup cleanup = new Cleanup();
    private final Cors cors = new Cors();
    private final Config config = new Config();

    @Data
    public static class SuperAdmin {
        private String phoneNumber;
    }

    @Data
    public static class Vault {
        private String keyId;
        private String vaultPath;
        private String algorithm;
        private int keySize;
    }

    @Data
    public static class Storage {
        private final ExpirationTimeMin expirationTimeMin = new ExpirationTimeMin();
        private final Times times = new Times();

        @Data
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
        public static class Times {
            private int failedAttempts;
            private int maxOtpPerIpPerHour;
            private int maxGlobalOtpPerMinute;
            private int maxVerificationAttemptsPerHour;
        }
    }

    @Data
    public static class Cleanup {
        private final Authorization authorization = new Authorization();

        @Data
        public static class Authorization {
            private Long cutoff;
        }
    }

    @Data
    public static class Cors {
        private String allowedOrigins;
        private String allowedMethods;
        private String allowedHeaders;
        private String exposedHeaders;
        private boolean allowedCredentials;
        private long maxAge;
    }

    @Data
    public static class Config {
        private String language;
    }
}