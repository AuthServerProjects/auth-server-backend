package com.behpardakht.oauth_server.authorization.model.mapper;

import com.behpardakht.oauth_server.authorization.model.dto.user.UsersDto;
import com.behpardakht.oauth_server.authorization.model.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserMapper {

    public UsersDto toDto(Users entity) {
        if (entity == null) return null;
        return UsersDto.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .phoneNumber(entity.getPhoneNumber())
                .isEnabled(entity.getIsEnabled())
                .build();
    }

    public List<UsersDto> toDtoList(List<Users> entityList) {
        return Optional.ofNullable(entityList).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(this::toDto).toList();
    }

    public Users toEntity(UsersDto dto) {
        if (dto == null) return null;
        return Users.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .phoneNumber(dto.getPhoneNumber())
                .isEnabled(dto.getIsEnabled())
                .build();
    }

    public void toEntity(Users entity, UsersDto dto) {
        entity.setUsername(dto.getUsername());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setIsEnabled(dto.getIsEnabled());
    }

    public List<Users> toEntityList(List<UsersDto> dtoList) {
        return Optional.ofNullable(dtoList).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(this::toEntity).toList();
    }
}