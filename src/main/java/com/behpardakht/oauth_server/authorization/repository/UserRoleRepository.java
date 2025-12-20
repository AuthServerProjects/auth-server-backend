package com.behpardakht.oauth_server.authorization.repository;

import com.behpardakht.oauth_server.authorization.model.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long>, JpaSpecificationExecutor<UserRole> {

    boolean existsByUserClientIdAndRoleId(Long userClientId, Long roleId);

    boolean existsByRoleId(Long roleId);

    Optional<UserRole> findByUserClientIdAndRoleId(Long userClientId, Long roleId);

    List<UserRole> findByUserClientId(Long userClientId);

    List<UserRole> findByRoleId(Long roleId);

    @Query("SELECT ra FROM UserRole ra " +
            "WHERE ra.userClient.user.username = :username " +
            "AND ra.userClient.client.id = :clientId")
    List<UserRole> findByUsernameAndClientId(@Param("username") String username,
                                             @Param("clientId") Long clientId);

    @Query("SELECT ra FROM UserRole ra " +
            "WHERE ra.userClient.client.id = :clientId")
    List<UserRole> findByClientId(@Param("clientId") Long clientId);
}