package com.behpardakht.oauth_server.authorization.repository;

import com.behpardakht.oauth_server.authorization.model.entity.Permission;
import com.behpardakht.oauth_server.authorization.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    boolean existsByNameAndClientId(String name, Long clientId);

    boolean existsByPermissions_id(Long permissionsId);

    Optional<Role> findByNameAndClientId(String name, Long clientId);
}