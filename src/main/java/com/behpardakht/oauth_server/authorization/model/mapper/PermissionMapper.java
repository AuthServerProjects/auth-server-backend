package com.behpardakht.oauth_server.authorization.model.mapper;

import com.behpardakht.oauth_server.authorization.model.dto.role.PermissionDto;
import com.behpardakht.oauth_server.authorization.model.entity.Permission;
import com.behpardakht.oauth_server.authorization.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PermissionMapper {

    private final ClientMapper clientMapper;
    private final PermissionRepository permissionRepository;

    public PermissionDto toDto(Permission entity) {
        if (entity == null) return null;
        return PermissionDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .clientId(entity.getClient().getId())
                .build();
    }

    public Permission toEntity(PermissionDto dto) {
        if (dto == null) return null;
        return Permission.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .client(clientMapper.loadEntity(dto.getClientId()))
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

    public Set<Permission> loadEntitySet(Set<PermissionDto> dtoSet) {
        Set<Long> ids = dtoSet.stream().map(PermissionDto::getId).collect(Collectors.toSet());
        return permissionRepository.findByIdIn(ids);
    }
}