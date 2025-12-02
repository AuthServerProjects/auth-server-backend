package com.behpardakht.oauth_server.authorization.model.mapper;

import com.behpardakht.oauth_server.authorization.model.dto.user.UsersDto;
import com.behpardakht.oauth_server.authorization.model.entity.Users;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@AllArgsConstructor
public class UserMapper {

    private final RoleMapper roleMapper;

    public UsersDto toDto(Users entity) {
        if (entity != null) {
            return UsersDto.builder()
                    .id(entity.getId())
                    .username(entity.getUsername())
                    .password(entity.getPassword())
                    .phoneNumber(entity.getPhoneNumber())
                    .isAccountNonExpired(entity.getIsAccountNonExpired())
                    .isAccountNonLocked(entity.getIsAccountNonLocked())
                    .isCredentialsNonExpired(entity.getIsCredentialsNonExpired())
                    .isEnabled(entity.getIsEnabled())
                    .roles(roleMapper.toDtoList(entity.getRoles()))
                    .build();
        }
        return null;
    }

    public List<UsersDto> toDtoList(List<Users> entityList) {
        return Optional.ofNullable(entityList).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(this::toDto).toList();
    }

    public Users toEntity(UsersDto dto) {
        if (dto != null) {
            return Users.builder()
                    .username(dto.getUsername())
                    .password(dto.getPassword())
                    .phoneNumber(dto.getPhoneNumber())
                    .isAccountNonExpired(dto.getIsAccountNonExpired())
                    .isAccountNonLocked(dto.getIsAccountNonLocked())
                    .isCredentialsNonExpired(dto.getIsCredentialsNonExpired())
                    .isEnabled(dto.getIsEnabled())
                    .roles(roleMapper.loadEntityList(dto.getRoles()))
                    .build();
        }
        return null;
    }

    public List<Users> toEntityList(List<UsersDto> dtoList) {
        return Optional.ofNullable(dtoList).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(this::toEntity).toList();
    }
}