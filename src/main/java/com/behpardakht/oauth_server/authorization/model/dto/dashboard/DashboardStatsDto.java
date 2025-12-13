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
public class DashboardStatsDto {
    private Long failedOtpLastHour;
    private Long failedOtpToday;
    private Long successfulLoginsToday;
    private Long sessionsRevokedToday;
    private List<TopFailedIpDto> topFailedIps;
    private List<RecentActivityDto> recentActivity;
}