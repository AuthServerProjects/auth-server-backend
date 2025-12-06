package com.behpardakht.oauth_server.authorization.model.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationDto {
    private Long id;
    private String authorizationId;
    private String principalName;
    private String registeredClientId;
    private Instant accessTokenIssuedAt;
    private Instant accessTokenExpiresAt;
    private Instant refreshTokenIssuedAt;
    private Instant refreshTokenExpiresAt;
}