package com.behpardakht.oauth_server.authorization.repository;

import com.behpardakht.oauth_server.authorization.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    boolean existsByName(String name);

    Optional<Role> findByName(String name);

    @Query("SELECT COUNT(user) > 0 FROM Users user JOIN user.roles role WHERE role.id = :roleId")
    boolean isRoleAssignedToUsers(@Param("roleId") Long roleId);
}