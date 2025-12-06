package com.behpardakht.oauth_server.authorization.model.mapper;

import com.behpardakht.oauth_server.authorization.model.dto.RoleDto;
import com.behpardakht.oauth_server.authorization.model.entity.Role;
import com.behpardakht.oauth_server.authorization.repository.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class RoleMapper {

    private final RoleRepository roleRepository;

    public RoleDto toDto(Role entity) {
        if (entity != null) {
            return RoleDto.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .build();
        }
        return null;
    }

    public Set<RoleDto> toDtoList(Set<Role> entityList) {
        return Optional.ofNullable(entityList).orElse(Collections.emptySet())
                .stream().filter(Objects::nonNull).map(this::toDto).collect(Collectors.toSet());
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
        return Optional.ofNullable(dtoList).orElse(Collections.emptySet())
                .stream().filter(Objects::nonNull).map(this::toEntity).collect(Collectors.toSet());
    }

    public Role loadEntity(RoleDto dto) {
        if (dto != null) {
            return roleRepository.findByName(dto.getName()).orElse(null);
        }
        return null;
    }

    public Set<Role> loadEntityList(Set<RoleDto> dtoList) {
        return Optional.ofNullable(dtoList).orElse(Collections.emptySet())
                .stream().filter(Objects::nonNull).map(this::loadEntity).collect(Collectors.toSet());
    }
}