package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.model.dto.dashboard.ClientDashboardStatsDto;
import com.behpardakht.oauth_server.authorization.model.dto.dashboard.DashboardStatsDto;
import com.behpardakht.oauth_server.authorization.model.dto.dashboard.RecentActivityDto;
import com.behpardakht.oauth_server.authorization.model.dto.dashboard.TopFailedIpDto;
import com.behpardakht.oauth_server.authorization.model.entity.AuditLog;
import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;
import com.behpardakht.oauth_server.authorization.repository.AuditLogRepository;
import com.behpardakht.oauth_server.authorization.repository.UserClientAssignmentRepository;
import com.behpardakht.oauth_server.authorization.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AuditLogRepository auditLogRepository;
    private final UserClientAssignmentRepository userClientAssignmentRepository;

    public DashboardStatsDto getStats() {
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant todayStart = Instant.now().truncatedTo(ChronoUnit.DAYS);

        return DashboardStatsDto.builder()
                .failedOtpLastHour(countFailedOtp(oneHourAgo))
                .failedOtpToday(countFailedOtp(todayStart))
                .successfulLoginsToday(countSuccessfulLogins(todayStart))
                .sessionsRevokedToday(countSessionsRevoked(todayStart))
                .topFailedIps(getTopFailedIps(todayStart))
                .recentActivity(getRecentActivity())
                .build();
    }

    private Long countFailedOtp(Instant after) {
        return auditLogRepository.countByActionAndSuccessAndCreatedAtAfter(
                AuditAction.OTP_VERIFIED, false, after);
    }

    private Long countSuccessfulLogins(Instant after) {
        return auditLogRepository.countByActionAndSuccessAndCreatedAtAfter(
                AuditAction.OTP_VERIFIED, true, after);
    }

    private Long countSessionsRevoked(Instant after) {
        return auditLogRepository.countByActionInAndCreatedAtAfter(
                List.of(AuditAction.SESSION_REVOKED, AuditAction.ALL_SESSION_REVOKED), after);
    }

    private List<TopFailedIpDto> getTopFailedIps(Instant after) {
        return auditLogRepository.findTopFailedIps(after).stream()
                .map(row -> TopFailedIpDto.builder()
                        .ipAddress((String) row[0])
                        .failedCount((Long) row[1])
                        .build())
                .toList();
    }

    private List<RecentActivityDto> getRecentActivity() {
        return auditLogRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(this::toRecentActivityDto)
                .toList();
    }

    private RecentActivityDto toRecentActivityDto(AuditLog log) {
        return RecentActivityDto.builder()
                .action(log.getAction())
                .username(log.getUsername())
                .clientName(log.getClient() != null ? log.getClient().getClientId() : null)
                .ipAddress(log.getIpAddress())
                .success(log.getSuccess())
                .createdAt(log.getCreatedAt())
                .build();
    }

    public ClientDashboardStatsDto getClientStats() {
        Long clientId = SecurityUtils.getCurrentClientId();
        Instant todayStart = Instant.now().truncatedTo(ChronoUnit.DAYS);

        return ClientDashboardStatsDto.builder()
                .activeUsers(userClientAssignmentRepository.countByClientIdAndIsEnabledTrue(clientId))
                .loginsToday(countSuccessfulLoginsByClient(clientId, todayStart))
                .failedLoginsToday(countFailedLoginsByClient(clientId, todayStart))
                .sessionsRevokedToday(countSessionsRevokedByClient(clientId, todayStart))
                .recentActivity(getRecentActivityByClient(clientId))
                .build();
    }

    private Long countSuccessfulLoginsByClient(Long clientId, Instant after) {
        return auditLogRepository.countByActionAndSuccessAndClientIdAndCreatedAtAfter(
                AuditAction.OTP_VERIFIED, true, clientId, after);
    }

    private Long countFailedLoginsByClient(Long clientId, Instant after) {
        return auditLogRepository.countByActionAndSuccessAndClientIdAndCreatedAtAfter(
                AuditAction.OTP_VERIFIED, false, clientId, after);
    }

    private Long countSessionsRevokedByClient(Long clientId, Instant after) {
        return auditLogRepository.countByActionInAndClientIdAndCreatedAtAfter(
                List.of(AuditAction.SESSION_REVOKED, AuditAction.ALL_SESSION_REVOKED), clientId, after);
    }

    private List<RecentActivityDto> getRecentActivityByClient(Long clientId) {
        return auditLogRepository.findTop20ByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(this::toRecentActivityDto)
                .toList();
    }
}