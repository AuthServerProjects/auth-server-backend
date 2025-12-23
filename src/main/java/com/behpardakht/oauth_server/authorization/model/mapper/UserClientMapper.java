package com.behpardakht.oauth_server.authorization.model.mapper;

import com.behpardakht.oauth_server.authorization.model.dto.user.UserClientDto;
import com.behpardakht.oauth_server.authorization.model.entity.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserClientMapper {

    private final UserMapper userMapper;

    public UserClientDto toDto(UserClient entity) {
        if (entity == null) return null;
        return UserClientDto.builder()
                .id(entity.getId())
                .user(userMapper.toDto(entity.getUser()))
                .clientId(entity.getClient() != null ? entity.getClient().getId() : null)
                .clientName(entity.getClient() != null ? entity.getClient().getClientId() : null)
                .isEnabled(entity.getIsEnabled())
                .isAccountNonExpired(entity.getIsAccountNonExpired())
                .isAccountNonLocked(entity.getIsAccountNonLocked())
                .isCredentialsNonExpired(entity.getIsCredentialsNonExpired())
                .createdAt(LocalDateTime.ofInstant(entity.getCreatedAt(), ZoneId.systemDefault()))
                .build();
    }

    public List<UserClientDto> toDtoList(List<UserClient> entities) {
        return Optional.ofNullable(entities).orElse(Collections.emptyList()).stream().map(this::toDto).toList();
    }

    public void updateEntity(UserClient entity, UserClientDto dto) {
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