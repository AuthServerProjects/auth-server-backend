package com.behpardakht.oauth_server.authorization.model.mapper;

import com.behpardakht.oauth_server.authorization.model.dto.user.UserClientAssignmentDto;
import com.behpardakht.oauth_server.authorization.model.entity.UserClientAssignment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserClientAssignmentMapper {

    private final UserMapper userMapper;

    public UserClientAssignmentDto toDto(UserClientAssignment entity) {
        if (entity == null) return null;
        return UserClientAssignmentDto.builder()
                .id(entity.getId())
                .user(userMapper.toDto(entity.getUser()))
                .clientId(entity.getClient() != null ? entity.getClient().getId() : null)
                .clientName(entity.getClient() != null ? entity.getClient().getClientId() : null)
                .isEnabled(entity.getIsEnabled())
                .isAccountNonExpired(entity.getIsAccountNonExpired())
                .isAccountNonLocked(entity.getIsAccountNonLocked())
                .isCredentialsNonExpired(entity.getIsCredentialsNonExpired())
                .createdAt(LocalDateTime.from(entity.getCreatedAt()))
                .build();
    }

    public List<UserClientAssignmentDto> toDtoList(List<UserClientAssignment> entities) {
        return Optional.ofNullable(entities).orElse(Collections.emptyList()).stream().map(this::toDto).toList();
    }

    public void updateEntity(UserClientAssignment entity, UserClientAssignmentDto dto) {
        if (dto == null) return;
        if (dto.getIsEnabled() != null) {
            entity.setIsEnabled(dto.getIsEnabled());
        }
        if (dto.getIsAccountNonExpired() != null) {
            entity.setIsAccountNonExpired(dto.getIsAccountNonExpired());
        }
        if (dto.getIsAccountNonLocked() != null) {
            entity.setIsAccountNonLocked(dto.getIsAccountNonLocked());
        }
        if (dto.getIsCredentialsNonExpired() != null) {
            entity.setIsCredentialsNonExpired(dto.getIsCredentialsNonExpired());
        }
    }
}