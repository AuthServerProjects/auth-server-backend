package com.behpardakht.oauth_server.authorization.model.mapper;

import com.behpardakht.oauth_server.authorization.model.dto.role.RoleDto;
import com.behpardakht.oauth_server.authorization.model.entity.Role;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleMapper {

    private final ClientMapper clientMapper;
    private final PermissionMapper permissionMapper;

    public RoleDto toDto(Role entity) {
        if (entity != null) {
            return RoleDto.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .clientId(entity.getClient() != null ? entity.getClient().getId() : null)
                    .isEnabled(entity.getIsEnabled())
                    .permissions(permissionMapper.toDtoSet(entity.getPermissions()))
                    .build();
        }
        return null;
    }

    public List<RoleDto> toDtoList(List<Role> entityList) {
        return Optional.ofNullable(entityList).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(this::toDto).toList();
    }

    public Set<RoleDto> toDtoSet(Set<Role> roles) {
        return Optional.ofNullable(roles).orElse(Collections.emptySet())
                .stream().filter(Objects::nonNull).map(this::toDto).collect(Collectors.toSet());
    }

    public Role toEntity(RoleDto dto) {
        if (dto != null) {
            return Role.builder()
                    .name(dto.getName())
                    .client(clientMapper.loadEntity(dto.getClientId()))
                    .isEnabled(dto.getIsEnabled() != null ? dto.getIsEnabled() : true)
                    .permissions(permissionMapper.loadEntitySet(dto.getPermissions()))
                    .build();
        }
        return null;
    }

    public List<Role> toEntityList(List<RoleDto> dtoList) {
        return Optional.ofNullable(dtoList).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(this::toEntity).toList();
    }

    public void updateEntity(Role entity, RoleDto dto) {
        if (dto == null) return;
        if (Strings.isNotBlank(dto.getName())) {
            entity.setName(dto.getName());
        }
        if (dto.getIsEnabled() != null) {
            entity.setIsEnabled(dto.getIsEnabled());
        }
    }
}