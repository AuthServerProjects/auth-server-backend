package com.behpardakht.oauth_server.authorization.model.mapper;

import com.behpardakht.oauth_server.authorization.model.dto.UsersDto;
import com.behpardakht.oauth_server.authorization.model.entity.Users;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    public Set<UsersDto> toDtoList(Set<Users> entityList) {
        if (entityList != null && !entityList.isEmpty()) {
            return entityList.stream().filter(Objects::nonNull).map(this::toDto).collect(Collectors.toSet());
        }
        return new HashSet<>(0);
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

    public Set<Users> toEntityList(Set<UsersDto> dtoList) {
        if (dtoList != null && !dtoList.isEmpty()) {
            return dtoList.stream().filter(Objects::nonNull).map(this::toEntity).collect(Collectors.toSet());
        }
        return new HashSet<>(0);
    }
}