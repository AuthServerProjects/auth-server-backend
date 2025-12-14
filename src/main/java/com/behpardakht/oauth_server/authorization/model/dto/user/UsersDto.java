package com.behpardakht.oauth_server.authorization.model.dto.user;

import com.behpardakht.oauth_server.authorization.model.dto.role.RoleAssignmentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsersDto {
    private Long id;
    private String username;
    private String password;
    private String phoneNumber;
    private Boolean isAccountNonExpired;
    private Boolean isAccountNonLocked;
    private Boolean isCredentialsNonExpired;
    private Boolean isEnabled;
    private Set<RoleAssignmentDto> roleAssignments;
}