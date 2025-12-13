package com.behpardakht.oauth_server.authorization.model.dto.dashboard;

import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityDto {
    private AuditAction action;
    private String username;
    private String clientId;
    private String ipAddress;
    private Boolean success;
    private Instant createdAt;
}