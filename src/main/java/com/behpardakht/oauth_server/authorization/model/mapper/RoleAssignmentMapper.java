package com.behpardakht.oauth_server.authorization.model.mapper;

import com.behpardakht.oauth_server.authorization.model.dto.role.RoleAssignmentDto;
import com.behpardakht.oauth_server.authorization.model.entity.RoleAssignment;
import com.behpardakht.oauth_server.authorization.repository.RoleAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleAssignmentMapper {

    private final RoleAssignmentRepository roleAssignmentRepository;

    public RoleAssignmentDto toDto(RoleAssignment entity) {
        if (entity == null) return null;
        return RoleAssignmentDto.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .username(entity.getUser() != null ? entity.getUser().getUsername() : null)
                .roleId(entity.getRole() != null ? entity.getRole().getId() : null)
                .roleName(entity.getRole() != null ? entity.getRole().getName() : null)
                .build();
    }

    public List<RoleAssignmentDto> toDtoList(List<RoleAssignment> entities) {
        return Optional.ofNullable(entities).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(this::toDto).toList();
    }

    public Set<RoleAssignmentDto> toDtoSet(Set<RoleAssignment> entities) {
        return Optional.ofNullable(entities).orElse(Collections.emptySet())
                .stream().filter(Objects::nonNull).map(this::toDto).collect(Collectors.toSet());
    }

    public RoleAssignment loadEntity(RoleAssignmentDto dto) {
        if (dto != null) {
            return roleAssignmentRepository.findById(dto.getId()).orElse(null);
        }
        return null;
    }

    public Set<RoleAssignment> loadEntityList(Set<RoleAssignmentDto> dtoList) {
        return Optional.ofNullable(dtoList).orElse(Collections.emptySet())
                .stream().filter(Objects::nonNull).map(this::loadEntity).collect(Collectors.toSet());
    }
}