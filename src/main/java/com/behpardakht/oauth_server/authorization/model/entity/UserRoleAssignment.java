package com.behpardakht.oauth_server.authorization.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_role_assignment",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_client_assignment_id", "role_id"}))
public class UserRoleAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_client_assignment_id", nullable = false)
    private UserClientAssignment userClientAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}