package com.behpardakht.oauth_server.authorization.repository;

import com.behpardakht.oauth_server.authorization.model.entity.RoleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, Long> {

    @Query("SELECT ra FROM RoleAssignment ra " +
            "JOIN FETCH ra.role r " +
            "JOIN FETCH r.permissions " +
            "LEFT JOIN FETCH ra.client " +
            "WHERE ra.user.username = :username")
    List<RoleAssignment> findByUserUsername(@Param("username") String username);

    List<RoleAssignment> findByUserId(Long userId);

    List<RoleAssignment> findByRoleId(Long roleId);

    List<RoleAssignment> findByClientId(Long clientId);

    @Query("SELECT ra FROM RoleAssignment ra " +
            "WHERE ra.user.id = :userId " +
            "AND ra.role.id = :roleId " +
            "AND ra.client.id = :clientId")
    Optional<RoleAssignment> findByUserIdAndRoleIdAndClientId(@Param("userId") Long userId,
                                                              @Param("roleId") Long roleId,
                                                              @Param("clientId") Long clientId);

    @Query("SELECT CASE WHEN COUNT(ra) > 0 THEN true ELSE false END FROM RoleAssignment ra " +
            "WHERE ra.user.id = :userId " +
            "AND ra.role.id = :roleId " +
            "AND  ra.client.id = :clientId")
    boolean existsByUserIdAndRoleIdAndClientId(@Param("userId") Long userId,
                                               @Param("roleId") Long roleId,
                                               @Param("clientId") Long clientId);

    boolean existsByRoleId(Long roleId);
}