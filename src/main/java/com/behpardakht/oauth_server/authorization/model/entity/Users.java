package com.behpardakht.oauth_server.authorization.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Table(name = "Users")
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Users implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

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

    @Column(name = "is_enabled")
    private Boolean isEnabled;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> (GrantedAuthority) role::getName)
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
        return this.isEnabled != null ? this.isEnabled : true;
    }
}