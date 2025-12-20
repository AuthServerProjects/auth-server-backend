package com.behpardakht.oauth_server.authorization.model.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDashboardStatsDto {
    private Long activeUsers;
    private Long loginsToday;
    private Long failedLoginsToday;
    private Long sessionsRevokedToday;
    private List<RecentActivityDto> recentActivity;
}