package com.behpardakht.oauth_server.authorization.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Users")
@EqualsAndHashCode(callSuper = true)
public class Users extends BaseEntity implements UserDetails {

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "is_account_non_expired")
    private Boolean isAccountNonExpired;

    @Column(name = "is_account_non_locked")
    private Boolean isAccountNonLocked;

    @Column(name = "is_credentials_non_expired")
    private Boolean isCredentialsNonExpired;

    @OneToMany(mappedBy = "users", fetch = FetchType.EAGER)
    private Set<RoleAssignment> roleAssignments;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roleAssignments == null) return Collections.emptySet();
        return roleAssignments.stream().map(roleAssignment ->
                        new SimpleGrantedAuthority("ROLE_" + roleAssignment.getRole().getName()))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.isAccountNonExpired != null ? this.isAccountNonExpired : true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.isAccountNonLocked != null ? this.isAccountNonLocked : true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.isCredentialsNonExpired != null ? this.isCredentialsNonExpired : true;
    }

    @Override
    public boolean isEnabled() {
        return this.getIsEnabled() != null ? this.getIsEnabled() : true;
    }
}