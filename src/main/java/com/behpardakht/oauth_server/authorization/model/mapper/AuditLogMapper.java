package com.behpardakht.oauth_server.authorization.model.mapper;

import com.behpardakht.oauth_server.authorization.model.dto.audit.AuditLogDto;
import com.behpardakht.oauth_server.authorization.model.entity.AuditLog;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class AuditLogMapper {

    public AuditLogDto toDto(AuditLog entity) {
        if (entity == null) return null;
        return AuditLogDto.builder()
                .id(entity.getId())
                .action(entity.getAction())
                .username(entity.getUsername())
                .clientId(entity.getClientId())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .details(entity.getDetails())
                .success(entity.getSuccess())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<AuditLogDto> toDtoList(List<AuditLog> entities) {
        return Optional.ofNullable(entities).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(this::toDto).toList();
    }
}