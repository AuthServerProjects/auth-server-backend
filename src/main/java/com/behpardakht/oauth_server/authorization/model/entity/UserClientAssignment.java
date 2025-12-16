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
@EqualsAndHashCode(callSuper = true)
@Table(name = "user_client_assignment", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "client_id"}))
public class UserClientAssignment extends BaseEntity {

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

    @OneToMany(mappedBy = "userClientAssignment", fetch = FetchType.LAZY)
    private Set<UserRoleAssignment> userRoleAssignments;
}