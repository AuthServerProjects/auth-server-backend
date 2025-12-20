package com.behpardakht.oauth_server.authorization.model.dto.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleDto {
    private Long id;
    private Long userClientId;
    private Long userId;
    private String username;
    private Long roleId;
    private String roleName;
}