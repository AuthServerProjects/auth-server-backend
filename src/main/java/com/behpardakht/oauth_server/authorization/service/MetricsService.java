package com.behpardakht.oauth_server.authorization.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public void incrementOtpSent(String clientId, boolean success) {
        Counter.builder("auth.otp.sent")
                .tag("client_id", clientId != null ? clientId : "unknown")
                .tag("success", String.valueOf(success))
                .register(meterRegistry)
                .increment();
    }

    public void incrementOtpVerified(String clientId, boolean success) {
        Counter.builder("auth.otp.verified")
                .tag("client_id", clientId != null ? clientId : "unknown")
                .tag("success", String.valueOf(success))
                .register(meterRegistry)
                .increment();
    }

    public void incrementTokenIssued(String clientId, String grantType) {
        Counter.builder("auth.token.issued")
                .tag("client_id", clientId != null ? clientId : "unknown")
                .tag("grant_type", grantType != null ? grantType : "unknown")
                .register(meterRegistry)
                .increment();
    }

    public void incrementTokenRefreshed(String clientId) {
        Counter.builder("auth.token.refreshed")
                .tag("client_id", clientId != null ? clientId : "unknown")
                .register(meterRegistry)
                .increment();
    }

    public void incrementTokenRevoked(String clientId) {
        Counter.builder("auth.token.revoked")
                .tag("client_id", clientId != null ? clientId : "unknown")
                .register(meterRegistry)
                .increment();
    }

    public void incrementSessionRevoked() {
        Counter.builder("auth.session.revoked")
                .register(meterRegistry)
                .increment();
    }

    // Client Metrics
    public void incrementClientCreated() {
        Counter.builder("auth.client.created")
                .register(meterRegistry)
                .increment();
    }

    public void incrementClientUpdated() {
        Counter.builder("auth.client.updated")
                .register(meterRegistry)
                .increment();
    }

    public void incrementUserCreated() {
        Counter.builder("auth.user.created")
                .register(meterRegistry)
                .increment();
    }

    public void incrementUserUpdated() {
        Counter.builder("auth.user.updated")
                .register(meterRegistry)
                .increment();
    }

    public void incrementLoginSuccess(String clientId) {
        Counter.builder("auth.login")
                .tag("client_id", clientId != null ? clientId : "unknown")
                .tag("result", "success")
                .register(meterRegistry)
                .increment();
    }

    public void incrementLoginFailed(String clientId, String reason) {
        Counter.builder("auth.login")
                .tag("client_id", clientId != null ? clientId : "unknown")
                .tag("result", "failed")
                .tag("reason", reason != null ? reason : "unknown")
                .register(meterRegistry)
                .increment();
    }

    public void incrementRateLimitHit(String type) {
        Counter.builder("auth.rate_limit.hit")
                .tag("type", type)
                .register(meterRegistry)
                .increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopTimer(Timer.Sample sample, String metricName, String... tags) {
        sample.stop(Timer.builder(metricName)
                .tags(tags)
                .register(meterRegistry));
    }
}