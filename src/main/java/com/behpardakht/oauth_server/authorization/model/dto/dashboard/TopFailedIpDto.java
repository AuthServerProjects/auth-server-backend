package com.behpardakht.oauth_server.authorization.model.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopFailedIpDto {
    private String ipAddress;
    private Long failedCount;
}