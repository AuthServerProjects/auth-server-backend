package com.behpardakht.oauth_server.authorization.aspect;

import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;
import com.behpardakht.oauth_server.authorization.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class MetricsAspect {

    private final MetricsService metricsService;

    @AfterReturning(value = "@annotation(auditable)")
    public void recordSuccessMetric(JoinPoint joinPoint, Auditable auditable) {
        recordMetric(auditable.action(), true);
    }

    @AfterThrowing(value = "@annotation(auditable)")
    public void recordFailureMetric(JoinPoint joinPoint, Auditable auditable) {
        recordMetric(auditable.action(), false);
    }

    private void recordMetric(AuditAction action, boolean success) {
        switch (action) {
            case OTP_SENT -> metricsService.incrementOtpSent(null, success);
            case OTP_VERIFIED -> {
                metricsService.incrementOtpVerified(null, success);
                if (success) {
                    metricsService.incrementLoginSuccess(null);
                } else {
                    metricsService.incrementLoginFailed(null, "otp_invalid");
                }
            }
            case USER_CREATED -> {
                if (success) metricsService.incrementUserCreated();
            }
            case USER_UPDATED -> {
                if (success) metricsService.incrementUserUpdated();
            }
            case CLIENT_CREATED -> {
                if (success) metricsService.incrementClientCreated();
            }
            case CLIENT_UPDATED -> {
                if (success) metricsService.incrementClientUpdated();
            }
            case SESSION_REVOKED, ALL_SESSION_REVOKED -> {
                if (success) metricsService.incrementSessionRevoked();
            }
            default -> {
            }
        }
    }
}