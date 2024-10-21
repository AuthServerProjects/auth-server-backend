package com.behpardakht.side_pay.auth.model.mapper;

import com.behpardakht.side_pay.auth.model.dto.UsersDto;
import com.behpardakht.side_pay.auth.model.entity.Users;
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
            UsersDto dto = new UsersDto();
            dto.setId(entity.getId());
            dto.setUsername(entity.getUsername());
            dto.setPassword(entity.getPassword());
            dto.setIsAccountNonExpired(entity.getIsAccountNonExpired());
            dto.setIsAccountNonLocked(entity.getIsAccountNonLocked());
            dto.setIsCredentialsNonExpired(entity.getIsCredentialsNonExpired());
            dto.setIsEnabled(entity.getIsEnabled());
            dto.setRoles(roleMapper.toDtoList(entity.getRoles()));
            return dto;
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
            Users entity = new Users();
            entity.setUsername(dto.getUsername());
            entity.setPassword(dto.getPassword());
            entity.setIsAccountNonExpired(dto.getIsAccountNonExpired());
            entity.setIsAccountNonLocked(dto.getIsAccountNonLocked());
            entity.setIsCredentialsNonExpired(dto.getIsCredentialsNonExpired());
            entity.setIsEnabled(dto.getIsEnabled());
            entity.setRoles(roleMapper.loadEntityList(dto.getRoles()));
            return entity;
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