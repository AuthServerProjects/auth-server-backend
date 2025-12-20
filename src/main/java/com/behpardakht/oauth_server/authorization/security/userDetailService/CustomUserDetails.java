package com.behpardakht.oauth_server.authorization.security.userDetailService;

import com.behpardakht.oauth_server.authorization.model.entity.UserClient;
import com.behpardakht.oauth_server.authorization.model.entity.Users;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {


    private final Users user;
    private final UserClient assignment;

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public boolean isEnabled() {
        return assignment.getIsEnabled() != null ? assignment.getIsEnabled() : true;
    }

    @Override
    public boolean isAccountNonExpired() {
        return assignment.getIsAccountNonExpired() != null ? assignment.getIsAccountNonExpired() : true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return assignment.getIsAccountNonLocked() != null ? assignment.getIsAccountNonLocked() : true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return assignment.getIsCredentialsNonExpired() != null ? assignment.getIsCredentialsNonExpired() : true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (assignment.getUserRoles() == null) return Collections.emptySet();
        return assignment.getUserRoles().stream()
                .map(ra -> new SimpleGrantedAuthority("ROLE_" + ra.getRole().getName()))
                .collect(Collectors.toSet());
    }
}