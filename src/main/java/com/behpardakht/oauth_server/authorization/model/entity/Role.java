package com.behpardakht.oauth_server.authorization.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor

@Table(name = "Role")
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseEntity implements GrantedAuthority {

    public Role(String name) {
        this.name = name;
    }

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Override
    public String getAuthority() {
        return name;
    }
}