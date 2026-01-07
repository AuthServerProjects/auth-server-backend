package com.behpardakht.oauth_server.authorization.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_client", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "client_id"}))
public class UserClient extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Builder.Default
    @Column(name = "is_account_non_expired")
    private Boolean isAccountNonExpired = true;

    @Builder.Default
    @Column(name = "is_account_non_locked")
    private Boolean isAccountNonLocked = true;

    @Builder.Default
    @Column(name = "is_credentials_non_expired")
    private Boolean isCredentialsNonExpired = true;

    @OneToMany(mappedBy = "userClient", fetch = FetchType.LAZY)
    private Set<UserRole> userRoles;
}