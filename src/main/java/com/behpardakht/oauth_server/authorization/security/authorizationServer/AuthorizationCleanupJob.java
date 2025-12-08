package com.behpardakht.oauth_server.authorization.security.authorizationServer;

import com.behpardakht.oauth_server.authorization.config.Properties;
import com.behpardakht.oauth_server.authorization.repository.AuthorizationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationCleanupJob {

    private final Properties properties;
    private final AuthorizationRepository authorizationRepository;

    @Scheduled(fixedRateString = "${app.cleanup.authorization.rate}")
    @Transactional
    public void cleanupExpiredAuthorizationCodes() {
        try {
            Instant cutoff = Instant.now().minus(
                    properties.getCleanup().getAuthorization().getCutoff(), ChronoUnit.HOURS);
            int deleted = authorizationRepository.deleteByAuthorizationCodeExpiresAtBeforeAndAccessTokenIsNull(cutoff);
            if (deleted > 0) {
                log.info("Cleaned up {} expired authorization codes", deleted);
            }
        } catch (Exception e) {
            log.error("Error during authorization code cleanup", e);
        }
    }
}