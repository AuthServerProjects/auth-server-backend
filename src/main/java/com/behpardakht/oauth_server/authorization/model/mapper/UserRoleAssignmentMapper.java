package com.behpardakht.oauth_server.authorization.model.mapper;

import com.behpardakht.oauth_server.authorization.model.dto.role.UserRoleAssignmentDto;
import com.behpardakht.oauth_server.authorization.model.entity.UserRoleAssignment;
import com.behpardakht.oauth_server.authorization.repository.UserRoleAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserRoleAssignmentMapper {

    private final UserRoleAssignmentRepository userRoleAssignmentRepository;

    public UserRoleAssignmentDto toDto(UserRoleAssignment entity) {
        if (entity == null) return null;

        return UserRoleAssignmentDto.builder()
                .id(entity.getId())
                .userClientAssignmentId(entity.getUserClientAssignment().getId())
                .userId(entity.getUserClientAssignment().getUser().getId())
                .username(entity.getUserClientAssignment().getUser().getUsername())
                .roleId(entity.getRole().getId())
                .roleName(entity.getRole().getName())
                .build();
    }

    public List<UserRoleAssignmentDto> toDtoList(List<UserRoleAssignment> entities) {
        return Optional.ofNullable(entities).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(this::toDto).toList();
    }

    public Set<UserRoleAssignmentDto> toDtoSet(Set<UserRoleAssignment> entities) {
        return Optional.ofNullable(entities).orElse(Collections.emptySet())
                .stream().filter(Objects::nonNull).map(this::toDto).collect(Collectors.toSet());
    }

    public UserRoleAssignment loadEntity(UserRoleAssignmentDto dto) {
        if (dto != null) {
            return userRoleAssignmentRepository.findById(dto.getId()).orElse(null);
        }
        return null;
    }

    public Set<UserRoleAssignment> loadEntityList(Set<UserRoleAssignmentDto> dtoList) {
        return Optional.ofNullable(dtoList).orElse(Collections.emptySet())
                .stream().filter(Objects::nonNull).map(this::loadEntity).collect(Collectors.toSet());
    }
}