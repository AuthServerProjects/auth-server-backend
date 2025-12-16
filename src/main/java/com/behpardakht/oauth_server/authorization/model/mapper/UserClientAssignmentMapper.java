package com.behpardakht.oauth_server.authorization.model.mapper;

import com.behpardakht.oauth_server.authorization.model.dto.user.UserClientAssignmentDto;
import com.behpardakht.oauth_server.authorization.model.entity.UserClientAssignment;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class UserClientAssignmentMapper {
    public UserClientAssignmentDto toDto(UserClientAssignment entity) {
        if (entity == null) return null;

        return UserClientAssignmentDto.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .username(entity.getUser().getUsername())
                .phoneNumber(entity.getUser().getPhoneNumber())
                .clientId(entity.getClient() != null ? entity.getClient().getId() : null)
                .clientName(entity.getClient().getClientId())
                .isAccountNonExpired(entity.getIsAccountNonExpired())
                .isAccountNonLocked(entity.getIsAccountNonLocked())
                .isCredentialsNonExpired(entity.getIsCredentialsNonExpired())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<UserClientAssignmentDto> toDtoList(List<UserClientAssignment> entities) {
        return Optional.ofNullable(entities).orElse(Collections.emptyList()).stream().map(this::toDto).toList();
    }
}