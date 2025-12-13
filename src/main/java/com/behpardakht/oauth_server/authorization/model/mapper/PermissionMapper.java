package com.behpardakht.oauth_server.authorization.model.mapper;

import com.behpardakht.oauth_server.authorization.model.dto.PermissionDto;
import com.behpardakht.oauth_server.authorization.model.entity.Permission;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PermissionMapper {

    public PermissionDto toDto(Permission entity) {
        if (entity == null) return null;
        return PermissionDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }

    public Permission toEntity(PermissionDto dto) {
        if (dto == null) return null;
        return Permission.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }

    public Set<PermissionDto> toDtoSet(Set<Permission> entities) {
        return Optional.ofNullable(entities).orElse(Collections.emptySet())
                .stream().filter(Objects::nonNull).map(this::toDto).collect(Collectors.toSet());
    }

    public List<PermissionDto> toDtoList(List<Permission> entities) {
        return Optional.ofNullable(entities).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(this::toDto).toList();
    }

    public Set<Permission> toEntitySet(Set<PermissionDto> dtoSet) {
        return Optional.ofNullable(dtoSet).orElse(Collections.emptySet())
                .stream().filter(Objects::nonNull).map(this::toEntity).collect(Collectors.toSet());
    }

    public List<Permission> toEntityList(List<PermissionDto> dtoSet) {
        return Optional.ofNullable(dtoSet).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(this::toEntity).toList();
    }
}