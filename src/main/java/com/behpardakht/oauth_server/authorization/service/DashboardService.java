package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.model.dto.DashboardStatsDto;
import com.behpardakht.oauth_server.authorization.repository.AuthorizationRepository;
import com.behpardakht.oauth_server.authorization.repository.ClientRepository;
import com.behpardakht.oauth_server.authorization.repository.RoleRepository;
import com.behpardakht.oauth_server.authorization.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthorizationRepository authorizationRepository;

    public DashboardStatsDto getStats() {
        return DashboardStatsDto.builder()
                .totalClients(clientRepository.count())
                .activeClients(clientRepository.countByIsEnabledTrue())
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.countByIsEnabledTrue())
                .totalRoles(roleRepository.count())
                .activeSessions(authorizationRepository.countByAccessTokenExpiresAtAfter(Instant.now()))
                .build();
    }
}