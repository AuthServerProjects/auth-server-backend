package com.behpardakht.oauth_server.authorization.model.mapper;

import com.behpardakht.oauth_server.authorization.model.dto.RoleDto;
import com.behpardakht.oauth_server.authorization.model.entity.Role;
import com.behpardakht.oauth_server.authorization.repository.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class RoleMapper {

    private final RoleRepository roleRepository;

    public RoleDto toDto(Role entity) {
        if (entity != null) {
            RoleDto dto = new RoleDto();
            dto.setId(entity.getId());
            dto.setName(entity.getName());
            return dto;
        }
        return null;
    }

    public Set<RoleDto> toDtoList(Set<Role> entityList) {
        if (entityList != null && !entityList.isEmpty()) {
            return entityList.stream().filter(Objects::nonNull).map(this::toDto).collect(Collectors.toSet());
        }
        return new HashSet<>(0);
    }

    public Role toEntity(RoleDto dto) {
        if (dto != null) {
            Role entity = new Role();
            entity.setName(dto.getName());
            return entity;
        }
        return null;
    }

    public Set<Role> toEntityList(Set<RoleDto> dtoList) {
        if (dtoList != null && !dtoList.isEmpty()) {
            return dtoList.stream().filter(Objects::nonNull).map(this::toEntity).collect(Collectors.toSet());
        }
        return new HashSet<>(0);
    }

    public Role loadEntity(RoleDto dto) {
        if (dto != null) {
            return roleRepository.findRoleByName(dto.getName()).orElse(null);
        }
        return null;
    }

    public Set<Role> loadEntityList(Set<RoleDto> dtoList) {
        if (dtoList != null && !dtoList.isEmpty()) {
            return dtoList.stream().filter(Objects::nonNull).map(this::loadEntity).collect(Collectors.toSet());
        }
        return new HashSet<>(0);
    }
}