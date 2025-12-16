package com.behpardakht.oauth_server.authorization.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserClientAssignmentDto {
    private Long id;
    private Long userId;
    private String username;
    private String phoneNumber;
    private Long clientId;
    private String clientName;
    private Boolean isAccountNonExpired;
    private Boolean isAccountNonLocked;
    private Boolean isCredentialsNonExpired;
    private Boolean isEnabled;
    private Instant createdAt;
}