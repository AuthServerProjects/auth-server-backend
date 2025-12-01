package com.behpardakht.oauth_server.authorization.model.mapper;

import com.behpardakht.oauth_server.authorization.model.dto.auth.AuthorizationDto;
import com.behpardakht.oauth_server.authorization.model.entity.Authorizations;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class AuthorizationMapper {

    public AuthorizationDto toDto(Authorizations entity) {
        if (entity != null) {
            return AuthorizationDto.builder()
                    .id(entity.getId())
                    .authorizationId(entity.getAuthorizationId())
                    .principalName(entity.getPrincipalName())
                    .registeredClientId(entity.getRegisteredClientId())
                    .accessTokenIssuedAt(entity.getAccessTokenIssuedAt())
                    .accessTokenExpiresAt(entity.getAccessTokenExpiresAt())
                    .refreshTokenIssuedAt(entity.getRefreshTokenIssuedAt())
                    .refreshTokenExpiresAt(entity.getRefreshTokenExpiresAt())
                    .build();
        }
        return null;
    }

    public List<AuthorizationDto> toDtoList(List<Authorizations> entities) {
        return Optional.ofNullable(entities).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(this::toDto).toList();
    }
}