package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.model.dto.DashboardStatsDto;
import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;
import com.behpardakht.oauth_server.authorization.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthorizationRepository authorizationRepository;
    private final AuditLogRepository auditLogRepository;

    public DashboardStatsDto getStats() {
        Instant todayStart = Instant.now().truncatedTo(ChronoUnit.DAYS);
        return DashboardStatsDto.builder()
                .totalClients(clientRepository.count())
                .activeClients(clientRepository.countByIsEnabledTrue())
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.countByIsEnabledTrue())
                .totalRoles(roleRepository.count())
                .activeSessions(authorizationRepository.countByAccessTokenExpiresAtAfter(Instant.now()))
                .todayLogins(auditLogRepository.countByActionAndCreatedAtAfter(AuditAction.LOGIN_SUCCESS, todayStart))
                .todayFailedLogins(auditLogRepository
                        .countByActionAndSuccessAndCreatedAtAfter(AuditAction.LOGIN_FAILED, false, todayStart))
                .todayOtpSent(auditLogRepository.countByActionAndCreatedAtAfter(AuditAction.OTP_SENT, todayStart))
                .build();


    }
}