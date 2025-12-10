package com.behpardakht.oauth_server.authorization.model.dto.audit;

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
public class AuditLogDto {
    private Long id;
    private AuditAction action;
    private String username;
    private String clientId;
    private String ipAddress;
    private String userAgent;
    private String details;
    private Boolean success;
    private Instant createdAt;
}