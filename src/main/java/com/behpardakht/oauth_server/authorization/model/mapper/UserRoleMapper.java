package com.behpardakht.oauth_server.authorization.model.mapper;

import com.behpardakht.oauth_server.authorization.model.dto.role.UserRoleDto;
import com.behpardakht.oauth_server.authorization.model.entity.UserRole;
import com.behpardakht.oauth_server.authorization.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserRoleMapper {

    private final UserRoleRepository userRoleRepository;

    public UserRoleDto toDto(UserRole entity) {
        if (entity == null) return null;

        return UserRoleDto.builder()
                .id(entity.getId())
                .userClientId(entity.getUserClient().getId())
                .userId(entity.getUserClient().getUser().getId())
                .username(entity.getUserClient().getUser().getUsername())
                .roleId(entity.getRole().getId())
                .roleName(entity.getRole().getName())
                .isEnabled(entity.getIsEnabled())
                .build();
    }

    public List<UserRoleDto> toDtoList(List<UserRole> entities) {
        return Optional.ofNullable(entities).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(this::toDto).toList();
    }

    public Set<UserRoleDto> toDtoSet(Set<UserRole> entities) {
        return Optional.ofNullable(entities).orElse(Collections.emptySet())
                .stream().filter(Objects::nonNull).map(this::toDto).collect(Collectors.toSet());
    }

    public UserRole loadEntity(UserRoleDto dto) {
        if (dto != null) {
            return userRoleRepository.findById(dto.getId()).orElse(null);
        }
        return null;
    }

    public Set<UserRole> loadEntityList(Set<UserRoleDto> dtoList) {
        return Optional.ofNullable(dtoList).orElse(Collections.emptySet())
                .stream().filter(Objects::nonNull).map(this::loadEntity).collect(Collectors.toSet());
    }
}