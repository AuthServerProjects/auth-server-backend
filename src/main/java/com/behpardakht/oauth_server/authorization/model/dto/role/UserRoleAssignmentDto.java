package com.behpardakht.oauth_server.authorization.model.dto.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleAssignmentDto {
    private Long id;
    private Long userClientAssignmentId;
    private Long userId;
    private String username;
    private Long roleId;
    private String roleName;
}