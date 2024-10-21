package com.behpardakht.side_pay.auth.model.dto;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class UsersDto {
    private Long id;
    private String username;
    private String password;
    private String phoneNumber;
    private Boolean isAccountNonExpired;
    private Boolean isAccountNonLocked;
    private Boolean isCredentialsNonExpired;
    private Boolean isEnabled;
    private Set<RoleDto> roles;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> (GrantedAuthority) role::getName)
                .collect(Collectors.toSet());
    }
}