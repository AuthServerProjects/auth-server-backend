package com.behpardakht.oauth_server.authorization.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private Long totalClients;
    private Long activeClients;
    private Long totalUsers;
    private Long activeUsers;
    private Long totalRoles;
    private Long activeSessions;
}